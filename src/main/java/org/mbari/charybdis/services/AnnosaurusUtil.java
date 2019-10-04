package org.mbari.charybdis.services;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.helidon.config.Config;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.mbari.vars.services.NoopAuthService;
import org.mbari.vars.services.gson.AnnotationCreator;
import org.mbari.vars.services.gson.ByteArrayConverter;
import org.mbari.vars.services.gson.DurationConverter;
import org.mbari.vars.services.gson.TimecodeConverter;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoService;
import org.mbari.vars.services.impl.annosaurus.v1.AnnoWebServiceFactory;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.ImagedMoment;
import org.mbari.vcr4j.time.Timecode;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Brian Schlining
 * @since 2019-10-04T10:35:00
 */
public class AnnosaurusUtil {

    private Gson gson;
    private final String endpoint;
    private final AnnoService service;
    private final Logger log = Logger.getLogger(getClass().getName());
    private final OkHttpClient client = new OkHttpClient();

    public AnnosaurusUtil(Config config) {
        endpoint = config.get("annotation.service.url").asString().orElse("http://localhost:8084");
        var timeout = config.get("media.service.timeout").as(Duration.class).orElse(Duration.ofSeconds(30));
        var authService = new NoopAuthService();
        var serviceFactory = new AnnoWebServiceFactory(endpoint, timeout);
        service = new AnnoService(serviceFactory, authService);
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
        var url = endpoint + "/fast/details/" + linkName + "/" + linkValue;
//        var client = HttpClient.newHttpClient();
//        var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
//        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//            .thenApply(response -> {
//                var body = response.body();
//                log.info("Request completed\n" + body);
//                try {
//                    var annotations = jsonToAnnotations(body);
//                    log.info("Found " + annotations.size() + " annotations");
//                    return annotations;
//                }
//                catch (Exception e) {
//                    log.log(Level.WARNING, e, () -> "Fuck");
//                    return Collections.emptyList();
//                }
//
//            });

        Request request = new Request.Builder().url(url).build();
        try (var response = client.newCall(request).execute()) {
            var json = response.body().string();
            var annotations = jsonToAnnotations(json);
            return CompletableFuture.completedFuture(annotations);
        }
        catch (IOException e) {
            log.log(Level.WARNING, e, () -> "Failed to communicate with annosaurus");
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        catch (Exception e) {
            log.log(Level.WARNING, e, () -> "Failed to convert json to annotations");
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

    }
}
