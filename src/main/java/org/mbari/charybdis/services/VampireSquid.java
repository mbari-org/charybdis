package org.mbari.charybdis.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.mbari.charybdis.etc.rxjava.AsyncUtils;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.vampiresquid.sdk.r1.MediaService;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-10-04T10:51:00
 */
public class VampireSquid {

    private final MediaService service;
    private final Duration timeout;

    public VampireSquid(MediaService service, Duration timeout) {
        this.service = service;
        this.timeout = timeout;
    }

    public MediaService getService() {
        return service;
    }

    public List<Media> findMediaForAnnotations(List<Annotation> annotations) {
        var mediaUuids = annotations.stream()
                .map(Annotation::getVideoReferenceUuid)
                .distinct()
                .collect(Collectors.toList());

        Log.info("Starting lookup of " + mediaUuids.size() + " media");

        try {
            return AsyncUtils.collectAll(mediaUuids, service::findByUuid)
                    .exceptionally( e -> {
                        Log.warn("Failed to fetch media", e);
                        return Collections.emptyList();
                    })
                    .thenApply(ArrayList::new)
                    .get(timeout.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Media> findMediaByVideoSequenceName(String videoSequencename) {
        try {
            return service.findByVideoSequenceName(videoSequencename).get(timeout.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.warn("Failed to fetch media for videosequence named " + videoSequencename, e);
            throw new RuntimeException(e);
        }

    }

    public List<Media> findMediaByVideoFileName(String videoFileName) {
        try {
            return service.findByFilename(videoFileName).get(timeout.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.warn("Failed to fetch media for file named " + videoFileName, e);
            throw new RuntimeException(e);
        }
    }

}
