package org.fenixhub.resources;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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
import org.fenixhub.utils.Helpers;

@Path("/apps")
public class AppResource {

    @Inject
    private AppService appService;

    @Inject
    private Helpers helpers;

    /*
     * HEAD /apps/{appId}
     * 
     * Returns the metadata of the app.
     * 
     * @param appId The id of the app.
     * 
     * @return The metadata of the app.
     */
    // @HEAD
    // @CacheResult(cacheName = "app-info")
    // @Produces(MediaType.TEXT_PLAIN)
    // @Path("/{appId}")
    // public Response getAppMetadata(
    //     @PathParam("appId") Long appId
    // ) {
    //     AppMetadataDto appMetadataDto = appService.getAppMetadata(appId);
    //     return Response.ok()
    //         .header("Accept-Ranges", helpers.RANGE_UNITS)
    //         .header("X-App-Size", appMetadataDto.getSize())
    //         .tag(appMetadataDto.getHash())
    //         .build();
    // }

    /*
     * GET /apps/{appId}/info
     * 
     * Get the app info.
     *
     * @param appId the app id
     * 
     * @return the app info
     */
    // @GET
    // @CacheResult(cacheName = "app-info")
    // @Produces(MediaType.APPLICATION_JSON)
    // @Path("/{appId}/info")
    // public Response getAppInfo(
    //     @PathParam("appId") Long appId
    // ) {
    //     AppDto appDto = appService.getAppInfo(appId);
    //     return Response.ok(appDto)
    //     .build();
    // }


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
    @Path("/{appId}/info")
    public Response updateApp(
        @PathParam("appId") Long appId,
        @NotNull AppDto appDto
    ) {
        appService.updateApp(appId, appDto);
        return Response.noContent().build();
    }

}