package org.fenixhub.resource;

import io.quarkus.cache.CacheResult;
import org.fenixhub.dto.AppChunkDto;
import org.fenixhub.dto.ArchiveDto;
import org.fenixhub.dto.ChunkMetadataDto;
import org.fenixhub.dto.views.ArchiveView;
import org.fenixhub.service.ArchiveService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/archives")
public class ArchiveResource {
    
    @Inject ArchiveService archiveService;

    @GET
    @Path("")
    public Response getArchives(
        @QueryParam ("appId") @NotNull @Min(value = 0) Integer appId
    ) {
        return Response.ok(archiveService.getArchives(appId)).build();
    }

    @POST
    @Path("/initialize")
    @RolesAllowed({"developer"})
    public Response initializeAppArchive(
        @Valid @ConvertGroup(to = ArchiveView.Initialize.class) ArchiveDto archiveDto
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
     * @param base64data The chunk of the archive in Base64 encoding
     */
    @PATCH
    @Consumes("text/plain")
    @Path("/{archiveId}/chunks")
    @RolesAllowed({"developer"})
    public Response uploadChunk(
        @PathParam("archiveId") String archiveId,
        @HeaderParam("X-Chunk-Size") @NotNull @Min(value = 1) Integer chunkSize,
        @HeaderParam("X-Chunk-Index") @NotNull int chunkIndex,
        @HeaderParam("X-Chunk-Hash") @NotNull String chunkHash,
        @HeaderParam("X-Check-Integrity") boolean checkIntegrity,
        @NotBlank String base64data
    ) {
        archiveService.saveAppChunk(archiveId, chunkSize, chunkIndex, chunkHash, checkIntegrity, base64data);
        return Response.ok().build();
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
     * @param base64data The chunk of the archive in Base64 encoding
     */
    @PATCH
    @Consumes("message/byterange")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{archiveId}/chunks")
    @RolesAllowed({"developer"})
    public Response uploadChunk(
        @PathParam("archiveId") String archiveId,
        @HeaderParam("X-Chunk-Size") @NotNull @Min(value = 1) Integer chunkSize,
        @HeaderParam("X-Chunk-Index") @NotNull int chunkIndex,
        @HeaderParam("X-Chunk-Hash") @NotNull String chunkHash,
        @HeaderParam("X-Check-Integrity") boolean checkIntegrity,
        @NotEmpty byte[] base64bytes
    ) {
        archiveService.saveAppChunk(archiveId, chunkSize, chunkIndex, chunkHash, checkIntegrity, base64bytes);
        return Response.ok().build();
    }


    /*
     * GET /archives/{archiveId}/chunks/{chunkIndex}
     * 
     * Get information about an archive's chunks.
     * 
     * @param archiveId The id of the archive
     * 
     * @return The app chunk
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{archiveId}/chunks")
    public Response getChunks(
        @PathParam("archiveId") String archiveId
    ) {
        
        List<ChunkMetadataDto> appChunks = archiveService.getArchiveChunks(archiveId);
        return Response.ok(appChunks).build();
    }

    /*
     * GET /archives/{archiveId}/chunks/{chunkIndex}
     * 
     * Download a chunk of the app in Base64 encoded format.
     * 
     * @param archiveId The id of the archive
     * @param chunkIndex The index of the chunk to download
     * 
     * @return The app chunk
     */
    @GET
    @CacheResult(cacheName = "app-download-chunk")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("application/x-tar")
    @Path("/{archiveId}/chunks/{chunkIndex}")
    public Response downloadChunk(
        @PathParam("archiveId") String archiveId,
        @PathParam("chunkIndex") int chunkIndex
    ) {
        
        AppChunkDto appChunk = archiveService.getArchiveChunk(archiveId, chunkIndex);
        
        return Response
            .ok(appChunk.getData())
            .tag(appChunk.getHash())
            // // Specify the range of the chunk in the response header "Content-Range"
            // .header("Content-Range",
            //     helpers.RANGE_UNITS + " " + appChunk.getChunkIndexes()[0] + "-" + appChunk.getChunkIndexes()[1] + 
            //     "/" + appChunk.getAppSize())
            // Specify the hash of the chunk in the response header "X-Chunk-Hash"
            .header("X-Chunk-Hash", "base64sha256:" + appChunk.getHash())
            // Specify the total size of the downloading chunk in the response header "X-Chunk-Size"
            .header("X-Chunk-Size", appChunk.getChunkSize())
            // Specify the index of the downloading chunk over the total number of chunks in the response header "X-Chunks-Count"
            .header("X-Chunks-Count", chunkIndex + "/" + appChunk.getChunksCount())
            // Specify how the chunk should be served in the response header "Content-Disposition"
            .header("Content-Disposition", "attachment; filename=\""+appChunk.getAppArchive()+"\"")
            // Specify the encoding of the chunk in the response header "Content-Encoding"
            // .header("Content-Encoding", appChunk.getEncoding())
            .build();
    }



}
