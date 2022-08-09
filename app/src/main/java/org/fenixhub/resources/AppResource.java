package org.fenixhub.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PATCH;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fenixhub.dto.AppDto;
import org.fenixhub.services.AppService;

import io.quarkus.cache.CacheResult;

@Path("/apps")
public class AppResource {

    @Inject AppService appService;

    /*
     * GET /apps/{appId}/info
     * 
     * Get the app info.
     *
     * @param appId the app id
     * 
     * @return the app info
     */
    @GET
    @CacheResult(cacheName = "app-info")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{appId}")
    public Response getAppInfo(
        @PathParam("appId") Integer appId
    ) {
        AppDto appDto = appService.getApp(appId);
        return Response.ok(appDto)
        .build();
    }


    /*
     * PUT /apps
     * 
     * Register a new app.
     * 
     * @param appDto The app to register.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    @RolesAllowed("DEVELOPER")
    public Response registerApp(
        @NotNull AppDto appDto
    ) {
        AppDto app = appService.registerApp(appDto);
        URI location;
        try {
            location = new URI("/app/"+app.getId().toString());
        } catch (URISyntaxException e) {
            throw new InternalServerErrorException("Could not create URI for app.", e);
        }
        return Response.ok(app).location(location).build();
    }

    /*
     * PATCH /apps/{appId}/info
     * 
     * Update the app info.
     */
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{appId}")
    @RolesAllowed("DEVELOPER")
    public Response updateApp(
        @PathParam("appId") Integer appId,
        @NotNull AppDto appDto
    ) {
        appService.updateApp(appId, appDto);
        return Response.noContent().build();
    }

}