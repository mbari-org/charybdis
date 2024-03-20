package org.mbari.charybdis.api;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.mbari.charybdis.domain.CountByMedia;
import org.mbari.charybdis.services.Annosaurus;
import org.mbari.charybdis.services.AnnotationServiceConfig;
import org.mbari.charybdis.services.VampireSquid;
import org.mbari.vars.core.util.AsyncUtils;


import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Path("/count")
public class SimpleCounts {

    @Inject
    AnnotationServiceConfig config;

    @Inject
    Annosaurus annosaurus;

    @Inject
    VampireSquid vampireSquid;


    @GET
    @Path("/dive/{videoSequenceName}")
    public CountByMedia countMediaByVideoSequenceName(String videoSequenceName) throws ExecutionException, InterruptedException, TimeoutException {
        var media = vampireSquid.findMediaByVideoSequenceName(videoSequenceName);
        var service = annosaurus.getService();
        var future = AsyncUtils.collectAll(media, m -> service.countAnnotations(m.getVideoReferenceUuid()))
                .thenApply(annotationCounts -> {
                            long count = annotationCounts.stream()
                                    .mapToLong(ac -> ac.getCount().longValue())
                                    .sum();
                            return new CountByMedia(count, new ArrayList<>(annotationCounts));
                        }
                );
        return future.get(config.getTimeout().getSeconds(), TimeUnit.SECONDS);
    }


}
