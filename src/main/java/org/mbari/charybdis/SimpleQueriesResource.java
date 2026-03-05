package org.mbari.charybdis;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.mbari.charybdis.domain.DataGroup;
import org.mbari.charybdis.services.DataGroupService;

@Path("/query")
public class SimpleQueriesResource {

    @Inject
    DataGroupService dataGroupService;

    @GET
    @Path("/concept/{concept}")
    @Produces(MediaType.APPLICATION_JSON)
    public DataGroup queryByConcept(@PathParam("concept") String concept,
                                    @DefaultValue("10000") @QueryParam("limit") Integer limit,
                                    @DefaultValue("0") @QueryParam("offset") Integer offset) {
        limit = limit == null || limit < 0 || limit > 10000 ? 10000 : limit;
        offset = offset == null || offset < 0 ? 0 : offset;
        return dataGroupService.findByConcept(concept, limit, offset);
    }

    @GET
    @Path("/dive/{videoSequenceName}")
    @Produces(MediaType.APPLICATION_JSON)
    public DataGroup queryByDive(@PathParam("videoSequenceName") String videoSequenceName,
                                 @DefaultValue("10000") @QueryParam("limit") Integer limit,
                                 @DefaultValue("0") @QueryParam("offset") Integer offset) {
        limit = limit == null || limit < 0 || limit > 10000 ? 10000 : limit;
        offset = offset == null || offset < 0 ? 0 : offset;
        return dataGroupService.findByDive(videoSequenceName, limit, offset);
    }

    @GET
    @Path("/file/{videoFileName}")
    @Produces(MediaType.APPLICATION_JSON)
    public DataGroup queryByVideoFileName(@PathParam("videoFileName") String videoFileName,
                                         @DefaultValue("10000") @QueryParam("limit") Integer limit,
                                         @DefaultValue("0") @QueryParam("offset") Integer offset) {
        limit = limit == null || limit < 0 || limit > 10000 ? 10000 : limit;
        offset = offset == null || offset < 0 ? 0 : offset;
        return dataGroupService.findByFilename(videoFileName, limit, offset);
    }
}
