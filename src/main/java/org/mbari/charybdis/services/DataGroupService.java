package org.mbari.charybdis.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mbari.charybdis.domain.DataGroup;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@ApplicationScoped
public class DataGroupService {


    @Inject
    Annosaurus annosaurus;

    @Inject
    VampireSquid vampireSquid;

    private record LimitOffset(Long limit, Long offset) {
        boolean isOk() {
            return offset >= 0 || limit > 0;
        }
    }

    private record MediaPage(UUID videoReferenceUuid, LimitOffset limitOffset) {}

    public DataGroup findByConcept(String concept, long limit, long offset) {
        var annotations = annosaurus.findByConcept(concept, limit, offset);
        var media = vampireSquid.findMediaForAnnotations(annotations);
        return new DataGroup(annotations, media);
    }

    public DataGroup findByDive(String videoSequenceName, long limit, long offset) {
        var media = vampireSquid.findMediaByVideoSequenceName(videoSequenceName);
        return limitedRequest(media, limit, offset).join();
    }

    public DataGroup findByFilename(String filename, long limit, long offset) {
        var media = vampireSquid.findMediaByVideoFileName(filename);
        return limitedRequest(media, limit, offset).join();
    }

    public CompletableFuture<DataGroup> limitedRequest(List<Media> media, long limit, long offset) {
        var cumSum = new AtomicLong(0);
        var returned = new AtomicLong(0);
        var annotations = new CopyOnWriteArrayList<Annotation>();

        var sortedMedia = media.stream()
                .sorted(Comparator.comparing(Media::getStartTimestamp))
                .collect(Collectors.toList());

        var service = annosaurus.getService();

        var queryFuture = AsyncUtils.collectAll(sortedMedia, m -> service.countAnnotations(m.getVideoReferenceUuid()))
                .thenCompose(annotationCounts -> {
                    var stream = annotationCounts.stream()
                            .map(ac -> {
                                var cumulativeCounts = cumSum.getAndAdd(ac.getCount());
                                var returnedCounts = returned.get();
                                var oi = calcOffset(cumulativeCounts, ac.getCount(), offset);
                                var li = calcLimit(returnedCounts, ac.getCount(), limit, oi);
                                if (oi >= 0L && li > 0L) {
                                    returned.addAndGet(li);
                                }
                                return new MediaPage(ac.getVideoReferenceUuid(), new LimitOffset(li, oi));
                            })
                            .filter(mediaPage -> mediaPage.limitOffset().isOk())
                            .map(mediaPage -> service.findAnnotations(mediaPage.videoReferenceUuid(),
                                    mediaPage.limitOffset.limit(),
                                    mediaPage.limitOffset.offset()).thenAccept(annotations::addAll))
                            .toList();
                    var array = stream.toArray(CompletableFuture[]::new);
                    return CompletableFuture.allOf(array);
                });

        return queryFuture.thenApply(v -> {
                    var ms = vampireSquid.findMediaForAnnotations(annotations);
                    return new DataGroup(annotations, ms);
                });
    }

    private long calcOffset(long cumulativeCounts, long thisCount, long offset) {
        var offsetIdx = offset - cumulativeCounts - 1;
        if (offsetIdx >= thisCount) {
            return -1;
        }
        else if (offsetIdx < 0) {
            return 0;
        }
        else {
            return offsetIdx;
        }
    }

    private long calcLimit(long returnedCounts, long thisCount, long limit, long thisOffset) {
        var a = thisCount - thisOffset;
        var r = limit - returnedCounts;
        return Math.min(a, r);
    }


}
