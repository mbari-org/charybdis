package org.mbari.charybdis.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.mbari.charybdis.domain.DataGroup;
import org.mbari.charybdis.services.Annosaurus;
import org.mbari.charybdis.services.DataGroupService;
import org.mbari.charybdis.services.VampireSquid;

@Path("/query")
public class SimpleQueries {

    @Inject
    Annosaurus annosaurus;

    @Inject
    VampireSquid vampireSquid;

    @Inject
    DataGroupService dataGroupService;

    @GET
    @Path("/concept/{concept}")
    @Produces(MediaType.APPLICATION_JSON)
    public DataGroup queryByConcept(String concept,
                                    @PathParam("limit") Integer limit,
                                    @PathParam("offset") Integer offset) {
        limit = limit == null || limit < 0 || limit > 5000 ? 100 : limit;
        offset = offset == null || offset < 0 ? 0 : offset;
        return dataGroupService.findByConcept(concept, limit, offset);
    }

    @GET
    @Path("/dive/{videoSequenceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public DataGroup queryByDive(String videoSequenceName,
                                 @PathParam("limit") Integer limit,
                                 @PathParam("offset") Integer offset) {
        limit = limit == null || limit < 0 || limit > 5000 ? 100 : limit;
        offset = offset == null || offset < 0 ? 0 : offset;
        return dataGroupService.findByDive(videoSequenceName, limit, offset);
    }

    @GET
    @Path("/file/{videoFileName}")
    @Produces(MediaType.APPLICATION_JSON)
    public DataGroup queryByVideoFileName(String videoFileName,
                                         @PathParam("limit") Integer limit,
                                         @PathParam("offset") Integer offset) {
        limit = limit == null || limit < 0 || limit > 5000 ? 100 : limit;
        offset = offset == null || offset < 0 ? 0 : offset;
        return dataGroupService.findByFilename(videoFileName, limit, offset);
    }
}
