package org.fenixhub.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fenixhub.dto.AppChunkDto;
import org.fenixhub.dto.AppDto;
import org.fenixhub.dto.AppMetadataDto;
import org.fenixhub.services.AppService;

import io.quarkus.cache.CacheResult;

@Path("/app")
public class AppResource {

    @Inject
    private AppService appService;

    @HEAD
    @CacheResult(cacheName = "app-info")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{appName}")
    public Response getAppMetadata(
        @PathParam("appName") String appName
    ) {
        AppMetadataDto appMetadataDto = appService.getAppMetadata(appName);
        return Response.ok()
            .header("Accept-Ranges", appService.RANGE_UNITS)
            .header("X-App-Size", appMetadataDto.getSize())
            .tag(appMetadataDto.getHash())
            .build();
    }

    @GET
    @CacheResult(cacheName = "app-info")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{appName}/info")
    public Response getAppInfo(
        @PathParam("appName") String appName
    ) {
        AppDto appMetadata = appService.getAppInfo(appName);
        return Response.ok(appMetadata)
        .build();
    }

    @GET
    @CacheResult(cacheName = "app-download-chunk")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{appName}")
    public Response downloadChunk(
        @PathParam("appName") String appName,
        @HeaderParam("Range") String range
    ) {
        
        AppChunkDto appChunk = appService.getAppChunk(appName, range);

        long appChunks = appChunk.getAppSize() / appChunk.getData().length;
        long downloadingChunk = appChunk.getChunkIndexes()[0] / appChunk.getData().length;

        return Response
            .status(appChunk.getData().length < appChunk.getAppSize() ? Status.PARTIAL_CONTENT : Status.OK)
            .entity(Base64.getEncoder().encodeToString(appChunk.getData()))
            .tag(appChunk.getHash())
            .header("Content-Range",
                appService.RANGE_UNITS + " " + appChunk.getChunkIndexes()[0] + "-" + appChunk.getChunkIndexes()[1] + 
                "/" + appChunk.getAppSize())
            .header("X-Chunk-Size", appChunk.getData().length)
            .header("X-Chunks", downloadingChunk + "/" + appChunks)
            .header("Content-Disposition", "attachment; filename=\""+appChunk.getAppArchive()+"\"")
            .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public Response setAppInfo(
        @Valid AppDto appInfoDto
    ) throws URISyntaxException {
        URI location = new URI("/app/"+appInfoDto.getName());
        return Response
            .ok()
            .location(location)
            .build();
    }

    @PATCH
    @Consumes("message/byterange")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{appName}")
    public Response uploadChunk(
        @PathParam("appName") String appName,
        @HeaderParam("X-Archive") @NotBlank String archive,
        @HeaderParam("Content-Range") String contentRange,
        @NotEmpty byte[] data
    ) {
        appService.saveAppChunk(appName, archive, contentRange, data);
        return Response.ok().build();
    }

}