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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.fenixhub.dto.PackageChunkDto;
import org.fenixhub.dto.PackageInfoDto;
import org.fenixhub.dto.PackageMetadataDto;
import org.fenixhub.services.PackageService;
import org.jboss.logging.Logger;

import io.quarkus.cache.CacheResult;
import io.smallrye.common.constraint.NotNull;

@Path("/package")
public class PackageResource {

    private static final Logger LOG = Logger.getLogger(PackageResource.class);

    @ConfigProperty(name = "package.range.units", defaultValue = "bytes")
    private String RANGE_UNITS;

    @Inject
    private PackageService packageService;

    @HEAD
    @CacheResult(cacheName = "package-info")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{packageName}")
    public Response getPackageMetadata(
        @PathParam("packageName") String packageName
    ) {
        PackageMetadataDto packageMetadataDto = packageService.getPackageMetadata(packageName);
        return Response.ok()
            .header("Accept-Ranges", RANGE_UNITS)
            .header("X-Package-Size", packageMetadataDto.getSize())
            .tag(packageMetadataDto.getHash())
            .build();
    }

    @GET
    @CacheResult(cacheName = "package-info")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{packageName}/info")
    public Response getPackageInfo(
        @PathParam("packageName") String packageName
    ) {
        PackageInfoDto packageMetadata = packageService.getPackageInfo(packageName);
        return Response.ok(packageMetadata)
        .build();
    }

    @GET
    @CacheResult(cacheName = "package-download-chunk")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{packageName}")
    public Response downloadChunk(
        @PathParam("packageName") String packageName,
        @HeaderParam("Range") String range
    ) {
        
        PackageChunkDto packageChunk = packageService.getPackageChunk(packageName, range);

        long packageChunks = packageChunk.getPackageSize() / packageChunk.getData().length;
        long downloadingChunk = packageChunk.getChunkIndexes()[0] / packageChunk.getData().length;

        return Response
            .status(packageChunk.getData().length < packageChunk.getPackageSize() ? Status.PARTIAL_CONTENT : Status.OK)
            .entity(Base64.getEncoder().encodeToString(packageChunk.getData()))
            .tag(packageChunk.getHash())
            .header("Content-Range",
                RANGE_UNITS + " " + packageChunk.getChunkIndexes()[0] + "-" + packageChunk.getChunkIndexes()[1] + 
                "/" + packageChunk.getPackageSize())
            .header("X-Chunk-Size", packageChunk.getData().length)
            .header("X-Chunks", downloadingChunk + "/" + packageChunks)
            .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public Response setPackageInfo(
        @Valid PackageInfoDto packageInfoDto
    ) throws URISyntaxException {
        URI location = new URI("/package/"+packageInfoDto.getName());
        return Response
            .ok()
            .location(location)
            .build();
    }

    @PATCH
    @Consumes("message/byterange")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{packageName}")
    public Response uploadChunk(
        @PathParam("packageName") String packageName,
        @HeaderParam("X-Archive") @NotBlank String archive,
        @HeaderParam("Content-Range") String contentRange,
        @NotEmpty byte[] data
    ) {
        packageService.savePackageChunk(packageName, archive, contentRange, data);
        return Response.ok().build();
    }

}