package org.mbari.charybdis.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mbari.jcommons.util.Logging;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.MediaService;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-10-04T10:51:00
 */
@ApplicationScoped
public class VampireSquid {

    @Inject
    MediaService service;

    @Inject
    MediaServiceConfig config;

    private final Logging log = new Logging(getClass());

    public MediaService getService() {
        return service;
    }

    public List<Media> findMediaForAnnotations(List<Annotation> annotations) {
        var mediaUuids = annotations.stream()
                .map(Annotation::getVideoReferenceUuid)
                .distinct()
                .collect(Collectors.toList());

        log.atInfo().log("Starting lookup of " + mediaUuids.size() + " media");

        try {
            return AsyncUtils.collectAll(mediaUuids, service::findByUuid)
                    .exceptionally( e -> {
                        log.atWarn().withCause(e).log(() -> "Failed to fetch media");
                        return Collections.emptyList();
                    })
                    .thenApply(ArrayList::new)
                    .get(config.timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Media> findMediaByVideoSequenceName(String videoSequencename) {
        try {
            return service.findByVideoSequenceName(videoSequencename).get(config.timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.atWarn().withCause(e).log(() -> "Failed to fetch media for videosequence named " + videoSequencename);
            throw new RuntimeException(e);
        }

    }

    public List<Media> findMediaByVideoFileName(String videoFileName) {
        try {
            return service.findByFilename(videoFileName).get(config.timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.atWarn().withCause(e).log(() -> "Failed to fetch media for file named " + videoFileName);
            throw new RuntimeException(e);
        }
    }

}
