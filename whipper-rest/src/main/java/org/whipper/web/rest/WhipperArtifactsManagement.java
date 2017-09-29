package org.whipper.web.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import org.whipper.web.artifact.WhipperArtifactsService;

@Path(WhipperArtifactsManagement.ARTIFACTS_ROOT)
@Api
public class WhipperArtifactsManagement{
    static final String ARTIFACTS_ROOT = "artifacts";

    @Inject
    private WhipperArtifactsService whipperArtifactsService;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArtifactsInfo(){
        // TODO
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
}
