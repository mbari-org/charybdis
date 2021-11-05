package org.mbari.charybdis.services;

import io.helidon.config.Config;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.NoopAuthService;
import org.mbari.vars.services.impl.vampiresquid.v1.VamService;
import org.mbari.vars.services.impl.vampiresquid.v1.VamWebServiceFactory;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-10-04T10:51:00
 */
public class VampireSquid {

    private final VamService service;
    private final Logger log = Logger.getLogger(getClass().getName());

    public VampireSquid(Config config) {
        var endpoint = config.get("media.service.url")
                .asString()
                .orElse("http://localhost:8082");
        var timeout = config.get("media.service.timeout")
                .as(Duration.class)
                .orElse(Duration.ofSeconds(30));
        var authService = new NoopAuthService();
        var serviceFactory = new VamWebServiceFactory(endpoint, timeout);
        service = new VamService(serviceFactory, authService);
    }

    public CompletableFuture<List<Media>> findMediaForAnnotations(List<Annotation> annotations) {
        var mediaUuids = annotations.stream()
                .map(Annotation::getVideoReferenceUuid)
                .distinct()
                .collect(Collectors.toList());

        log.info("Starting lookup of " + mediaUuids.size() + " media");

        return AsyncUtils.collectAll(mediaUuids, service::findByUuid)
                .exceptionally( e -> {
                    log.log(Level.WARNING, e, () -> "Failed to fetch media");
                    return Collections.emptyList();
                })
                .thenApply(ArrayList::new);
    }

    public CompletableFuture<List<Media>> findMediaByVideoSequenceName(String videoSequencename) {
        return service.findByVideoSequenceName(videoSequencename)
                .exceptionally(e -> {
                    log.log(Level.WARNING, e, () -> "Failed to fetch media for " + videoSequencename);
                    return Collections.emptyList();
                })
                .thenApply(ArrayList::new);
    }

    public CompletableFuture<List<Media>> findMediaByVideoFileName(String videoFileName) {
        return service.findByFilename(videoFileName)
            .exceptionally(e -> {
                log.log(Level.WARNING, e, () -> "Failed to fetch media with filename of " + videoFileName);
                return Collections.emptyList();
            })
            .thenApply(ArrayList::new);
    }

}
