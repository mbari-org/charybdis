package org.mbari.charybdis.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.mbari.charybdis.CountByDive;
import org.mbari.charybdis.CountByMedia;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.model.Media;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;


public class SimpleCountService implements HttpService {

    private final Annosaurus annosaurus;
    private final VampireSquid vampireSquid;


    public SimpleCountService(Annosaurus annosaurus, VampireSquid vampireSquid) {
        this.annosaurus = annosaurus;
        this.vampireSquid = vampireSquid;
    }

    @Override
    public void routing(HttpRules rules) {
        rules.options((req, res) -> {
        }); // Needed for CORS
        rules.get("/dive/{videoSequenceName}", this::byDiveHandler);
    }

    private void byDiveHandler(ServerRequest request, ServerResponse response) {
        response.headers().contentType(MediaTypes.APPLICATION_JSON);
        String videoSequenceName = request.path()
                .absolute()
                .pathParameters()
                .get("videoSequenceName");

        var result = vampireSquid.findMediaByVideoSequenceName(videoSequenceName)
                .thenCompose(this::countByMedia)
                .thenApply(c -> new CountByDive(c.count(), videoSequenceName, c.annotationCounts()))
                .thenApply(obj -> annosaurus.getGson().toJson(obj))
                .join();

        response.send(result);


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
