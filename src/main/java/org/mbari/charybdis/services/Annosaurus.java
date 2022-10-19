package org.mbari.charybdis.services;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.github.mizosoft.methanol.Methanol;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.helidon.config.Config;
import org.mbari.charybdis.domain.ExtendedAnnotation;
import org.mbari.vars.services.NoopAuthService;
import org.mbari.vars.services.Pager;
import org.mbari.vars.services.gson.AnnotationCreator;
import org.mbari.vars.services.gson.ByteArrayConverter;
import org.mbari.vars.services.gson.DurationConverter;
import org.mbari.vars.services.gson.TimecodeConverter;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoService;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.AnnotationCount;
import org.mbari.vars.services.model.ConceptCount;
import org.mbari.vars.services.model.ImagedMoment;
import org.mbari.vcr4j.time.Timecode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Brian Schlining
 * @since 2019-10-04T10:35:00
 */
public class Annosaurus {

    private Gson gson;
    private final String endpoint;
    private final AnnoService service;
    private final Logger log = Logger.getLogger(getClass().getName());
    private final Methanol httpClient;
    private final Duration timeout;
    private final Integer pageSize;

    public Annosaurus(Config config) {
        // Trim off trailing any trailing slashes
        var tempEndpoint = config.get("annotation.service.url").asString().orElse("http://localhost:8084");
        endpoint = tempEndpoint.endsWith("/") ? tempEndpoint.substring(0, tempEndpoint.length() - 1) : tempEndpoint;

        timeout = config.get("media.service.timeout").as(Duration.class).orElse(Duration.ofSeconds(30));
        pageSize = config.get("annotation.service.pagesize").asInt().orElse(2000);
        var authService = new NoopAuthService(); // Read-only
        var serviceFactory = new AnnoWebServiceFactory(endpoint, timeout);
        service = new AnnoService(serviceFactory, authService);
        httpClient = Methanol.newBuilder()
                .userAgent("Charybdis")
                .readTimeout(timeout)
                .connectTimeout(timeout)
                .autoAcceptEncoding(true)
                .build();
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
                    .registerTypeAdapter(byte[].class, new ByteArrayConverter());

            // Register java.time.Instant
            gson = Converters.registerInstant(gsonBuilder)
                    .create();
        }
        return gson;
    }

    public List<Annotation> jsonToAnnotations(String json) {
        return Arrays.asList(getGson().fromJson(json, Annotation[].class));
    }

    public CompletableFuture<List<Annotation>> findByLinkNameAndLinkValue(String linkName, String linkValue) {
        var url = endpoint + "/fast/details/" + linkName + "/" + linkValue + "?data=true";
        return call(url, this::jsonToAnnotations);
    }

    public CompletableFuture<List<Annotation>> findByLinkNameAndLinkValue(String linkName, String linkValue, long limit, long offset) {
        var url = endpoint + "/fast/details/" + linkName + "/" + linkValue + "?data=true&limit=" + limit + "&offset=" + offset;
        return call(url, this::jsonToAnnotations);
    }

    public CompletableFuture<AnnotationCount> countByVideoReferenceUuid(UUID videoReferenceUuid) {
        return service.countAnnotations(videoReferenceUuid);
    }

    public CompletableFuture<List<Annotation>> findByVideoReferenceUuid(UUID videoReferenceUuid) {
        var url = endpoint + "/fast/videoreference/" + videoReferenceUuid + "?data=true";
        return call(url, this::jsonToAnnotations);
    }

    public CompletableFuture<List<Annotation>> findByVideoReferenceUuid(UUID videoReferenceUuid, long limit, long offset) {
        var url = endpoint + "/fast/videoreference/" + videoReferenceUuid + "?data=true&limit=" + limit + "&offset=" + offset;
        return call(url, this::jsonToAnnotations);
    }

    public CompletableFuture<ConceptCount> countByConcept(String concept) {
        return service.countObservationsByConcept(concept);
    }

    public CompletableFuture<List<Annotation>> findByConcept(String concept, long limit, long offset)  {
        return service.findByConcept(concept, limit, offset, true);
    }

    public CompletableFuture<List<Annotation>> findByConcept(String concept)  {
        // HACK: Page size is hard coded
        var annotations = new CopyOnWriteArrayList<Annotation>();
        var future = new CompletableFuture<List<Annotation>>();

        try {
            var pager = countByConcept(concept).thenApply(conceptCount -> {
                return new Pager<List<Annotation>>((limit, offset) -> {
                    try {
                        // TODO sort annotations by time?
                        var annos =  service.findByConcept(concept, limit, offset, true)
                                .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                        log.info("Found " + annos.size() + " annotations");
                        return annos;
                    } catch (Exception e) {
                        log.log(Level.WARNING, e,
                                () -> "Failed to fetch annotation for " +
                                        concept + " (" + offset + "-" + (offset + limit) + ")");
                        return Collections.emptyList();
                    }
                }, conceptCount.getCount().longValue(), pageSize.longValue());

            }).get(timeout.toMillis(), TimeUnit.MILLISECONDS);

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

    public CompletableFuture<ConceptCount> countByQueryConstraint(String queryConstraintJson) {
        var url = "/fast/count";
        callWithJsonBody(url, queryConstraintJson)
    }

    public CompletableFuture<List<ExtendedAnnotation>> findByQueryConstraints(String queryConstraintJson) {
        var annotations = new CopyOnWriteArrayList<Annotation>();
        var future = new CompletableFuture<List<Annotation>>();
        try {

        }
        catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

    private <T> CompletableFuture<T> call(String url,
                                          Function<String, T> bodyConverter) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var json = response.body();
            var annotations = bodyConverter.apply(json);
            return CompletableFuture.completedFuture(annotations);
        }
        catch (IOException e) {
            log.log(Level.WARNING, e, () -> "Failed to communicate with annosaurus");
            return CompletableFuture.failedFuture(e);
        }
        catch (Exception e) {
            log.log(Level.WARNING, e, () -> "Failed to convert json to annotations");
            return CompletableFuture.failedFuture(e);
        }
    }

    private <T> CompletableFuture<T> callWithJsonBody(String url,
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
            return CompletableFuture.completedFuture(annotations);
        }
        catch (IOException e) {
            log.log(Level.WARNING, e, () -> "Failed to communicate with annosaurus");
            return CompletableFuture.failedFuture(e);
        }
        catch (Exception e) {
            log.log(Level.WARNING, e, () -> "Failed to convert json to annotations");
            return CompletableFuture.failedFuture(e);
        }
    }
}
