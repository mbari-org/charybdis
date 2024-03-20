package org.mbari.charybdis.services;


import com.github.mizosoft.methanol.Methanol;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mbari.charybdis.domain.DataGroup;
import org.mbari.jcommons.util.Logging;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.Pager;
import org.mbari.vars.services.gson.*;

import org.mbari.vars.services.model.*;
import org.mbari.vcr4j.time.Timecode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;


/**
 * @author Brian Schlining
 * @since 2019-10-04T10:35:00
 */
public class Annosaurus {


    private final Logging log = new Logging(getClass());
    private final HttpClient httpClient;
    private final Integer pageSize;
    private Gson gson;
    private final String endpoint;
    private final Duration timeout;
    private final AnnotationService service;

    public Annosaurus(AnnotationService annotationService, String endpoint, Duration timeout, Integer pageSize) {
        // Trim off trailing any trailing slashes
        this.service = annotationService;
        this.pageSize = pageSize;
        this.timeout = timeout;
        this.endpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        httpClient = Methanol.newBuilder()
                .userAgent("Charybdis")
                .readTimeout(timeout)
                .connectTimeout(timeout)
                .autoAcceptEncoding(true)
                .build();
        gson = getGson();
    }

    public AnnotationService getService() {
        return service;
    }

    public Gson getGson() {

        if (gson == null) {

            GsonBuilder gsonBuilder = new GsonBuilder()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .registerTypeAdapter(ImagedMoment.class, new AnnotationCreator())
                    .registerTypeAdapter(Duration.class, new DurationConverter())
                    .registerTypeAdapter(Timecode.class, new TimecodeConverter())
                    .registerTypeAdapter(Instant.class, new InstantConverter())
                    .registerTypeAdapter(byte[].class, new ByteArrayConverter());
            gson = gsonBuilder.create();
        }
        return gson;
    }

    public List<Annotation> jsonToAnnotations(String json) {
        return Arrays.asList(getGson().fromJson(json, Annotation[].class));
    }

    public MultiRequestCount countByVideoReferenceUuids(Collection<UUID> videoReferenceUuids) {
        var multiRequest = new MultiRequest(new ArrayList<>(videoReferenceUuids));
        if (videoReferenceUuids.isEmpty()) {
            return new MultiRequestCount(multiRequest, 0L);
        }
        try {
            return service.countByMultiRequest(multiRequest).get(timeout.getSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Annotation> findByLinkNameAndLinkValue(String linkName, String linkValue) {
        var url = endpoint + "/fast/details/" + linkName + "/" + linkValue + "?data=true";
        return call(url, this::jsonToAnnotations);
    }

    public List<Annotation> findByLinkNameAndLinkValue(String linkName, String linkValue, long limit, long offset) {
        var url = endpoint + "/fast/details/" + linkName + "/" + linkValue + "?data=true&limit=" + limit + "&offset=" + offset;
        return call(url, this::jsonToAnnotations);
    }

    public AnnotationCount countByVideoReferenceUuid(UUID videoReferenceUuid) {
        try {
            return service.countAnnotations(videoReferenceUuid).get(timeout.getSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Annotation> findByVideoReferenceUuid(UUID videoReferenceUuid) {
        var url = endpoint + "/fast/videoreference/" + videoReferenceUuid + "?data=true";
        return call(url, this::jsonToAnnotations);
    }

    public List<Annotation> findByVideoReferenceUuid(UUID videoReferenceUuid, long limit, long offset) {
        var url = endpoint + "/fast/videoreference/" + videoReferenceUuid + "?data=true&limit=" + limit + "&offset=" + offset;
        return call(url, this::jsonToAnnotations);
    }

    public ConceptCount countByConcept(String concept) {
        try {
            return service.countObservationsByConcept(concept).get(timeout.getSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Annotation> findByConcept(String concept, long limit, long offset)  {
        try {
            return service.findByConcept(concept, limit, offset, true).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<List<Annotation>> findByConcept(String concept)  {
        var annotations = new CopyOnWriteArrayList<Annotation>();
        var future = new CompletableFuture<List<Annotation>>();
        try {
            var count = countByConcept(concept);
            var pager = new Pager<List<Annotation>>((limit, offset) -> {
                try {
                    return findByConcept(concept, limit, offset);
                }
                catch (Exception e) {
                    log.atWarn().withCause(e).log(() -> "Failed to fetch annotation for " +
                            concept + " (" + offset + "-" + (offset + limit) + ")");
                    return Collections.emptyList();
                }
            }, count.getCount().longValue(), pageSize.longValue());

            pager.getObservable()
                    .subscribe(annotations::addAll,
                            future::completeExceptionally,
                            () -> future.complete(annotations));

            pager.run();
        }
        catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }



    private <T> T call(String url,
                                          Function<String, T> bodyConverter) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var json = response.body();
            var annotations = bodyConverter.apply(json);
            return annotations;
        }
        catch (IOException e) {
            log.atWarn().withCause(e).log(() -> "Failed to communicate with annosaurus");
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            log.atWarn().withCause(e).log(() -> "Failed to convert json to annotations");
            throw new RuntimeException(e);
        }
    }

    private <T> T callWithJsonBody(String url,
                                                  String jsonBody,
                                                  Function<String, T> bodyConverter) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var json = response.body();
            var annotations = bodyConverter.apply(json);
            return annotations;
        }
        catch (IOException e) {
            log.atWarn().withCause(e).log(() -> "Failed to communicate with annosaurus");
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            log.atWarn().withCause(e).log(() -> "Failed to convert json to annotations");
            throw new RuntimeException(e);
        }
    }
}
