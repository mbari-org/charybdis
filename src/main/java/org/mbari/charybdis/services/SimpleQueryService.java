package org.mbari.charybdis.services;

import io.helidon.common.http.MediaType;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import org.mbari.charybdis.DataGroup;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.Pager;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-11-13T15:03:00
 */
public class SimpleQueryService implements Service {

    private final AnnosaurusUtil annosaurusUtil;
    private final VampireSquidUtil vampireSquidUtil;
    private final Logger log = Logger.getLogger(getClass().getName());

    public SimpleQueryService(AnnosaurusUtil annosaurusUtil, VampireSquidUtil vampireSquidUtil) {
        this.annosaurusUtil = annosaurusUtil;
        this.vampireSquidUtil = vampireSquidUtil;

    }

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/dive/{videoSequenceName}", this::byDiveHandler);
        rules.get("/concept/{concept}", this::byConceptHandler);
    }

    private void byDiveHandler(ServerRequest request, ServerResponse response) {
        response.headers().contentType(MediaType.APPLICATION_JSON);
        String videoSequenceName = request.path()
                .absolute()
                .param("videoSequenceName");
        vampireSquidUtil.findMediaByVideoSequenceName(videoSequenceName)
                .thenAccept(ms -> {
                    var uuids = ms.stream()
                            .map(Media::getVideoReferenceUuid)
                            .collect(Collectors.toList());
                    AsyncUtils.collectAll(uuids, annosaurusUtil::findByVideoReferenceUuid)
                            .thenApply(annos -> annos.stream().flatMap(Collection::stream).collect(Collectors.toList()))
                            .thenApply(annos -> new DataGroup(annos, ms))
                            .thenApply(obj -> annosaurusUtil.getGson().toJson(obj))
                            .thenAccept(response::send);
                });
    }

    private void byConceptHandler(ServerRequest request, ServerResponse response) {
        response.headers().contentType(MediaType.APPLICATION_JSON);
        String concept = request.path()
                .absolute()
                .param("concept");
        annosaurusUtil.findByConcept(concept)
                .thenAccept(annos -> {
                    vampireSquidUtil.findMediaForAnnotations(annos)
                            .thenApply(ms -> new DataGroup(annos, ms))
                            .thenApply(obj -> annosaurusUtil.getGson().toJson(obj))
                            .thenAccept(response::send);
                });

    }
}