package org.fenixhub.resources;

import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fenixhub.dto.ArchiveDto;
import org.fenixhub.services.ArchiveService;

@Path("/archives")
public class ArchiveResource {
    
    @Inject
    private ArchiveService archiveService;

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public Response getArchivesByAppId(
        @QueryParam ("appId") @NotNull @Min(value = 0) Long appId
    ) {
        return Response.ok(archiveService.getArchivesByAppId(appId)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/initialize")
    public Response initializeAppArchive(
        ArchiveDto archiveDto
    ) {
        ArchiveDto uArchiveDto = archiveService.initializeAppArchive(archiveDto);
        return Response.ok(uArchiveDto).build();
    }

    /*
     * PATCH /archives/{archiveId}/chunks
     * 
     * Upload a chunk of the archive related to an existing app.
     * 
     * @param archiveId The id of the archive.
     * @param chunkIndex The "X-Chunk-Index" header, with the index of the chunk (ex. "1")
     * @param chunkSize The "X-Chunk-Size" header value, with the size of the chunk in bytes (ex. "1024")
     * @param chunkHash The "X-Chunk-Hash" header value, with the sha256 hash of the chunk (ex. "ac4623f63bafc15a4ceb615...")
     * @param data The chunk of the archive in Base64 encoding
     */
    @PATCH
    @Consumes("message/byterange")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{archiveId}/chunks")
    public Response uploadChunk(
        @PathParam("archiveId") String archiveId,
        @HeaderParam("X-Chunk-Size") @Min(value = 1) int chunkSize,
        @HeaderParam("X-Chunk-Index") @NotNull int chunkIndex,
        @HeaderParam("X-Chunk-Hash") @NotNull String chunkHash,
        @HeaderParam("X-Check-Integrity") boolean checkIntegrity,
        @NotEmpty byte[] data
    ) {
        archiveService.saveAppChunk(archiveId, chunkSize, chunkIndex, chunkHash, checkIntegrity, data);
        return Response.ok().build();
    }


    // /*
    //  * GET /archives/{archiveId}/chunks/{chunkIndex}
    //  * 
    //  * Download a chunk of the app in Base64 encoded format.
    //  * 
    //  * @param archiveId The id of the archive
    //  * @param chunkIndex The index of the chunk to download
    //  * 
    //  * @return The app chunk
    //  */
    // @GET
    // @CacheResult(cacheName = "app-download-chunk")
    // @Consumes(MediaType.APPLICATION_JSON)
    // @Produces("application/x-tar")
    // @Path("/{archiveId}/chunks/{chunkIndex}")
    // public Response downloadChunk(
    //     @PathParam("appId") Long appId,
    //     @PathParam("chunkIndex") Long chunkIndex
    // ) {
        
    //     AppChunkDto appChunk = appService.getAppChunk(appId, chunkIndex);
        
    //     return Response
    //         .ok(Base64.getEncoder().encodeToString(appChunk.getData()))
    //         .tag(appChunk.getHash())
    //         // // Specify the range of the chunk in the response header "Content-Range"
    //         // .header("Content-Range",
    //         //     helpers.RANGE_UNITS + " " + appChunk.getChunkIndexes()[0] + "-" + appChunk.getChunkIndexes()[1] + 
    //         //     "/" + appChunk.getAppSize())
    //         // Specify the total size of the downloading chunk in the response header "X-Chunk-Size"
    //         .header("X-Chunk-Size", appChunk.getData().length)
    //         // Specify the index of the downloading chunk over the total number of chunks in the response header "X-Chunks-Count"
    //         .header("X-Chunks-Count", (chunkIndex + 1) + "/" + appChunk.getChunksCount())
    //         // Specify how the chunk should be served in the response header "Content-Disposition"
    //         .header("Content-Disposition", "attachment; filename=\""+appChunk.getAppArchive()+"\"")
    //         // Specify the encoding of the chunk in the response header "Content-Encoding"
    //         .header("Content-Encoding", appService.compressionType)
    //         .build();
    // }



}
