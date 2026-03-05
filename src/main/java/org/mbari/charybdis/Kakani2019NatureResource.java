package org.mbari.charybdis;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.mbari.charybdis.domain.DataGroup;
import org.mbari.charybdis.services.Annosaurus;
import org.mbari.charybdis.services.VampireSquid;

import java.util.List;

@Path(("/n0"))
public class Kakani2019NatureResource {

    @Inject
    Annosaurus annosaurus;

    @Inject
    VampireSquid vampireSquid;

    @GET()
    @RunOnVirtualThread
    public DataGroup getNature() {
//        var annotations = annosaurus.findByLinkNameAndLinkValue("comment", "Nature20190609559");
//        var media = vampireSquid.findMediaForAnnotations(annotations);
//        return new DataGroup(annotations, media);
        return new DataGroup(List.of(), List.of());
    }

}
