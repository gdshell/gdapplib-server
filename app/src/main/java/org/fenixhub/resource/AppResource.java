package org.fenixhub.resource;

import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.http.HttpServerResponse;
import org.fenixhub.dto.AppDto;
import org.fenixhub.service.AppService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/apps")
public class AppResource {

    @Inject AppService appService;

    /*
     * GET /apps
     *
     * Returns a list of all apps.
     *
     * @return a list of all apps
     */
    @GET
    @Path("")
    @CacheResult(cacheName = "apps")
    public Uni<List<AppDto>> getApps() {
        return appService.getApps();
    }

    /*
     * POST /apps
     *
     * Register a new app.
     *
     * @param appDto The app to register.
     */
    @POST
    @Path("")
    @RolesAllowed("developer")
    public Uni<AppDto> registerApp(
            @Context HttpServerResponse response,
            @NotNull AppDto appDto
    ) {
        return appService.registerApp(appDto).map(
                Unchecked.function(app -> {
                    try {
                        response.putHeader(HttpHeaders.LOCATION, new URI("/apps/" + app.getId()).toString());
                    } catch (URISyntaxException e) {
                        throw new InternalServerErrorException("Failed to create app.");
                    }
                    return app;
                })
        );
    }


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
    @Path("/{appId}")
    public Uni<AppDto> getAppInfo(
        @PathParam("appId") Integer appId
    ) {
        return appService.getApp(appId);
    }


    /*
     * PATCH /apps/{appId}/info
     * 
     * Update the app info.
     */
    @PATCH
    @Path("/{appId}")
    @RolesAllowed({"developer", "admin"})
    public Uni<AppDto> updateApp(
        @PathParam("appId") Integer appId,
        @NotNull AppDto appDto
    ) {
        return appService.updateApp(appId, appDto);
    }

}