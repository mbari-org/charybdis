package org.mbari.charybdis.services;

import io.quarkus.logging.Log;
import org.mbari.charybdis.etc.rxjava.Pager;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.AnnotationCount;
import org.mbari.vars.annosaurus.sdk.r1.models.ConceptCount;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;


/**
 * @author Brian Schlining
 * @since 2019-10-04T10:35:00
 */
public class Annosaurus {

    private final AnnotationService service;
    private final Duration timeout;
    private final Integer pageSize;

    public Annosaurus(AnnotationService service, Duration timeout, Integer pageSize) {
        this.service = service;
        this.timeout = timeout;
        this.pageSize = pageSize;
        // Trim off trailing any trailing slashes
    }

    public AnnotationService getService() {
        return service;
    }

    public CompletableFuture<AnnotationCount> countByVideoReferenceUuid(UUID videoReferenceUuid) {
        return service.countAnnotations(videoReferenceUuid);
    }

    public CompletableFuture<List<Annotation>> findByVideoReferenceUuid(UUID videoReferenceUuid) {
        return service.findAnnotations(videoReferenceUuid);
    }

    public CompletableFuture<List<Annotation>> findByVideoReferenceUuid(UUID videoReferenceUuid, long limit, long offset) {
        return service.findAnnotations(videoReferenceUuid, limit, offset);
    }

    private static String encodeConcept(String concept) {
        return URLEncoder.encode(concept, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public CompletableFuture<ConceptCount> countByConcept(String concept) {
        return service.countObservationsByConcept(encodeConcept(concept));
    }

    public CompletableFuture<List<Annotation>> findByConcept(String concept, long limit, long offset)  {
        return service.findByConcept(encodeConcept(concept), limit, offset, true);
    }

    public CompletableFuture<List<Annotation>> findByConcept(String concept)  {
        // HACK: Page size is hard coded
        var encoded = encodeConcept(concept);
        var annotations = new CopyOnWriteArrayList<Annotation>();
        var future = new CompletableFuture<List<Annotation>>();

        try {
            var pager = service.countObservationsByConcept(encoded).thenApply(conceptCount -> {
                return new Pager<>((Long limit, Long offset) -> {
                    try {
                        // TODO sort annotations by time?
                        var annos =  service.findByConcept(encoded, limit, offset, true)
                                .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                        Log.info("Found " + annos.size() + " annotations");
                        return annos;
                    } catch (Exception e) {
                        Log.warn("Failed to fetch annotation for " +
                                encoded + " (" + offset + "-" + (offset + limit) + ")", e);
                        return Collections.emptyList();
                    }
                }, conceptCount.getCount().longValue(), pageSize.longValue());

            }).get(timeout.toMillis(), TimeUnit.MILLISECONDS);

            pager.getObservable()
                    .subscribe(xs -> annotations.addAll((Collection<? extends Annotation>) xs),
                            future::completeExceptionally,
                            () -> future.complete(annotations));
            pager.run();
        }
        catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }



}
