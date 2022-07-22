package org.fenixhub.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PATCH;
import javax.ws.rs.PUT;
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
import org.fenixhub.utils.Helpers;

import io.quarkus.cache.CacheResult;

@Path("/app")
public class AppResource {

    @Inject
    private AppService appService;

    @Inject
    private Helpers helpers;

    /*
     * HEAD /app/{appId}
     * 
     * Returns the metadata of the app.
     * 
     * @param appId The id of the app.
     * 
     * @return The metadata of the app.
     */
    @HEAD
    @CacheResult(cacheName = "app-info")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{appId}")
    public Response getAppMetadata(
        @PathParam("appId") Long appId
    ) {
        AppMetadataDto appMetadataDto = appService.getAppMetadata(appId);
        return Response.ok()
            .header("Accept-Ranges", helpers.RANGE_UNITS)
            .header("X-App-Size", appMetadataDto.getSize())
            .tag(appMetadataDto.getHash())
            .build();
    }

    /*
     * GET /app/{appId}/info
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
    @Path("/{appId}/info")
    public Response getAppInfo(
        @PathParam("appId") Long appId
    ) {
        AppDto appDto = appService.getAppInfo(appId);
        return Response.ok(appDto)
        .build();
    }


    /*
     * PUT /app
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
     * PATCH /app/{appId}/info
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

    /*
     * GET /app/{appId}/archive
     * 
     * Download a chunk of the app in Base64 encoded format.
     * 
     * @param appId The id of the app
     * @param range The "Range" header value (e.g. "bytes=0-1023")
     * 
     * @return The app chunk
     */
    @GET
    @CacheResult(cacheName = "app-download-chunk")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/x-tar")
    @Path("/{appId}")
    public Response downloadChunk(
        @PathParam("appId") Long appId,
        @HeaderParam("Range") String range
    ) {
        
        AppChunkDto appChunk = appService.getAppChunk(appId, range);

        long appChunks = appChunk.getAppSize() / appChunk.getData().length;
        long downloadingChunk = appChunk.getChunkIndexes()[0] / appChunk.getData().length;
        
        return Response
            .status(appChunk.getData().length < appChunk.getAppSize() ? Status.PARTIAL_CONTENT : Status.OK)
            .entity(Base64.getEncoder().encodeToString(appChunk.getData()))
            .tag(appChunk.getHash())
            // Specify the range of the chunk in the response header "Content-Range"
            .header("Content-Range",
                helpers.RANGE_UNITS + " " + appChunk.getChunkIndexes()[0] + "-" + appChunk.getChunkIndexes()[1] + 
                "/" + appChunk.getAppSize())
            // Specify the total size of the downloading chunk in the response header "X-Chunk-Size"
            .header("X-Chunk-Size", appChunk.getData().length)
            // Specify the index of the downloading chunk over the total number of chunks in the response header "X-Chunks-Count"
            .header("X-Chunks-Count", downloadingChunk + "/" + appChunks)
            // Specify how the chunk should be served in the response header "Content-Disposition"
            .header("Content-Disposition", "attachment; filename=\""+appChunk.getAppArchive()+"\"")
            // Specify the encoding of the chunk in the response header "Content-Encoding"
            .header("Content-Encoding", appService.compressionType)
            .build();
    }



    /*
     * PATCH /app/{appId}/archive
     * 
     * Upload a chunk of the archive related to an existing app.
     * 
     * @param appId The id of the app
     * @param archive The "X-Archive" header value, with the full name of the archive (ex. "app.tar.br")
     * @param contentRange The "Content-Range" header value, with the range of the chunk (ex. "bytes 0-10/100")
     * @param chunk The "X-Chunk" header value, with the index of the chunk (ex. "0")
     * @param data The chunk of the archive in Base64 encoding
     */
    @PATCH
    @Consumes("message/byterange")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{appId}/archive")
    public Response uploadChunk(
        @PathParam("appId") Long appId,
        @HeaderParam("X-Archive") @NotBlank String archive,
        @HeaderParam("Content-Range") String contentRange,
        @HeaderParam("X-Chunk") Long chunk,
        @NotEmpty byte[] data
    ) {
        appService.saveAppChunk(appId, archive, contentRange, data);
        return Response.ok().build();
    }

}