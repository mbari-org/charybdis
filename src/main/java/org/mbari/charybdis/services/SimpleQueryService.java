package org.mbari.charybdis.services;


import org.mbari.charybdis.DataGroup;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
// import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * @author Brian Schlining
 * @since 2019-11-13T15:03:00
 */
public class SimpleQueryService implements HttpService {

    private final Annosaurus annosaurus;
    private final VampireSquid vampireSquid;
    // private final Logger log = Logger.getLogger(getClass().getName());

    private record LimitOffset(Long limit, Long offset) {
        static Optional<LimitOffset> from(ServerRequest request) {

            var offset = request.query()
                    .first("offset")
                    .map(Long::parseLong)
                    .orElse(0L);
            return request.query()
                    .first("limit")
                    .map(Long::parseLong)
                    .map(limit -> new LimitOffset(limit, offset));
        }

        boolean isOk() {
            return offset >= 0 || limit > 0;
        }
    }

    private record MediaPage(UUID videoReferenceUuid, LimitOffset limitOffset) {}

    public SimpleQueryService(Annosaurus annosaurus, VampireSquid vampireSquid) {
        this.annosaurus = annosaurus;
        this.vampireSquid = vampireSquid;
    }

    @Override
    public void routing(HttpRules rules) {
        rules.options((req, res) -> {
        }); // Needed for CORS
        rules.get("/concept/{concept}", this::byConceptHandler);
        rules.get("/dive/{videoSequenceName}", this::byDiveHandler);
        rules.get("/file/{videoFileName}", this::byVideoFileNameHandler);
    }

    private void byQueryConstraintsHandler(ServerRequest request, ServerResponse response) {
        try {
            var body = request.content()
                    .as(String.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void countByDiveHandler(ServerRequest request, ServerResponse response) {
        
    }

    private void byDiveHandler(ServerRequest request, ServerResponse response) {
        var limitOffset = LimitOffset
                .from(request)
                .orElse(new LimitOffset(-1L, -1L));

        response.headers().contentType(MediaTypes.APPLICATION_JSON);
        String videoSequenceName = request.path()
                .absolute()
                .pathParameters()
                .get("videoSequenceName");

        var result = vampireSquid.findMediaByVideoSequenceName(videoSequenceName)
                .thenCompose(media -> limitedRequest(media, limitOffset))
                .thenApply(obj -> annosaurus.getGson().toJson(obj))
                .join();

        response.send(result);

    }

    private void byConceptHandler(ServerRequest request, ServerResponse response) {
        var limitOffset = LimitOffset.from(request);

        response.headers().contentType(MediaTypes.APPLICATION_JSON);
        String concept = request.path()
                .absolute()
                .pathParameters()
                .get("concept");
        var future = limitOffset
                .map(lo -> annosaurus.findByConcept(concept, lo.limit(), lo.offset()))
                .orElseGet(() -> annosaurus.findByConcept(concept));

        var result = future.thenCompose(annos ->
            vampireSquid.findMediaForAnnotations(annos)
                    .thenApply(ms -> new DataGroup(annos, ms))
                    .thenApply(obj -> annosaurus.getGson().toJson(obj))
        ).join();

        response.send(result);
    }

    private void byVideoFileNameHandler(ServerRequest request, ServerResponse response) {
        var limitOffset = LimitOffset
                .from(request)
                .orElse(new LimitOffset(-1L, -1L));

        response.headers().contentType(MediaTypes.APPLICATION_JSON);
        String videoFileName = request.path()
                .absolute()
                .pathParameters()
                .get("videoFileName");

        var result = vampireSquid.findMediaByVideoFileName(videoFileName)
                .thenCompose(media -> limitedRequest(media, limitOffset))
                .thenApply(obj -> annosaurus.getGson().toJson(obj))
                .join();

        response.send(result);

    }

    private static List<DataGroup> asDataGroups(Collection<Media> media, Collection<List<Annotation>> annotations) {
        var dataGroups = new ArrayList<DataGroup>();
        for (var m : media) {
            Optional<List<Annotation>> opt = annotations.stream()
                    .filter(xs -> !xs.isEmpty())
                    .filter(xs -> xs.get(0).getVideoReferenceUuid().equals(m.getVideoReferenceUuid()))
                    .findFirst();
            DataGroup dg = opt
                    .map(xs -> new DataGroup(xs, List.of(m)))
                    .orElseGet(() -> new DataGroup(Collections.emptyList(), List.of(m)));
            dataGroups.add(dg);
        }
        return dataGroups;
    }

    private CompletableFuture<DataGroup> limitedRequest(List<Media> media, LimitOffset limitOffset) {
        if (limitOffset == null || !limitOffset.isOk()) {
            var uuids = media.stream()
                    .sorted(Comparator.comparing(Media::getStartTimestamp))
                    .map(Media::getVideoReferenceUuid)
                    .collect(Collectors.toList());
            return AsyncUtils.collectAll(uuids, annosaurus::findByVideoReferenceUuid)
                    .thenApply(annos -> annos.stream().flatMap(Collection::stream).collect(Collectors.toList()))
                    .thenApply(annos -> new DataGroup(annos, media));
        }
        else {
            return limitedRequest(media, limitOffset.limit(), limitOffset.offset());
        }
    }

    private CompletableFuture<DataGroup> limitedRequest(List<Media> media, long limit, long offset) {
        var cumSum = new AtomicLong(0);
        var returned = new AtomicLong(0);
        var annotations = new CopyOnWriteArrayList<Annotation>();

        var sortedMedia = media.stream()
                .sorted(Comparator.comparing(Media::getStartTimestamp))
                .collect(Collectors.toList());

        var queryFuture = AsyncUtils.collectAll(sortedMedia, m -> annosaurus.countByVideoReferenceUuid(m.getVideoReferenceUuid()))
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
                            .map(mediaPage -> annosaurus.findByVideoReferenceUuid(mediaPage.videoReferenceUuid(),
                                    mediaPage.limitOffset.limit(),
                                    mediaPage.limitOffset.offset()).thenAccept(annotations::addAll))
                            .toList();
                    var array = stream.toArray(CompletableFuture[]::new);
                    return CompletableFuture.allOf(array);
                });

        return queryFuture.thenCompose(v ->
            vampireSquid.findMediaForAnnotations(annotations)
                    .thenApply(ms -> new DataGroup(annotations, ms))
        );
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
