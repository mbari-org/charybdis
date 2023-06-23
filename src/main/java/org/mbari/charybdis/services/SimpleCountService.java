package org.mbari.charybdis.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
// import java.util.logging.Logger;

import org.mbari.charybdis.CountByMedia;
import org.mbari.charybdis.CountByDive;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.model.Media;
import io.helidon.common.http.MediaType;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public class SimpleCountService implements Service {

    private final Annosaurus annosaurus;
    private final VampireSquid vampireSquid;
    // private final Logger log = Logger.getLogger(getClass().getName());


    public SimpleCountService(Annosaurus annosaurus, VampireSquid vampireSquid) {
        this.annosaurus = annosaurus;
        this.vampireSquid = vampireSquid;
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.options((req, res) -> {
        }); // Needed for CORS
        rules.get("/dive/{videoSequenceName}", this::byDiveHandler);
    }

    private void byDiveHandler(ServerRequest request, ServerResponse response) {
        response.headers().contentType(MediaType.APPLICATION_JSON);
        String videoSequenceName = request.path()
                .absolute()
                .param("videoSequenceName");

        vampireSquid.findMediaByVideoSequenceName(videoSequenceName)
                .thenCompose(this::countByMedia)
                .thenApply(c -> new CountByDive(c.count(), videoSequenceName, c.annotationCounts()))
                .thenApply(obj -> annosaurus.getGson().toJson(obj))
                .thenAccept(response::send);

    }

    private CompletableFuture<CountByMedia> countByMedia(List<Media> media) {
        return AsyncUtils.collectAll(media, m -> annosaurus.countByVideoReferenceUuid(m.getVideoReferenceUuid()))
                .thenApply(annotationCounts -> {
                            long count = annotationCounts.stream()
                                    .mapToLong(ac -> ac.getCount().longValue())
                                    .sum();
                            return new CountByMedia(count, new ArrayList<>(annotationCounts));
                        }
                );
    }


}
