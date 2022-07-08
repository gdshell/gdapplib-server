package org.fenixhub;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

import io.quarkus.cache.CacheResult;

@Path("/package")
public class PackageResource {

    private static final Logger LOG = Logger.getLogger(PackageResource.class);
    private static final String RANGE_UNITS = "bytes";

    @HEAD
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{packageName}")
    public Response size(
            @PathParam("packageName") String packageName) {
        return Response.ok()
                .header("Accept-Ranges", RANGE_UNITS)
                .header("Content-Length", getFileSize(packageName))
                .build();
    }

    public long getFileSize(String filePath) {
        long size = -1;
        try {
            size = Files.size(Paths.get(getClass().getResource("/" + filePath).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
        return size;
    }

    @GET
    @CacheResult(cacheName = "chunked-download")
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{packageName}")
    public Response downloadChunk(
            @PathParam("packageName") String packageName,
            @HeaderParam("Range") String range) {
        LOG.info("Range: " + range);
        long[] rangeLongValues = new long[2];

        URL resourceUrl = getClass().getResource("/" + packageName);
        long fileSize = getFileSize(resourceUrl);

        if (range != null) {
            String[] rangeArray = range.split("=");
            if (!RANGE_UNITS.equals(rangeArray[0])) {
                throw new BadRequestException("Server unable to handle " + rangeArray[0] + " range units.");
            }
            String[] rangeValues = rangeArray[1].split("-");
            rangeLongValues[0] = Math.min(Long.parseLong(rangeValues[0]), fileSize);
            rangeLongValues[1] = Math.min(Long.parseLong(rangeValues[1]), fileSize);

        } else {
            rangeLongValues[0] = 0;
            rangeLongValues[1] = (int) fileSize - 1;
        }

        byte[] bytes = new byte[(int) (rangeLongValues[1] - rangeLongValues[0] + 1)];
        InputStream inputStream = null;
        try {
            inputStream = resourceUrl.openStream();
            inputStream.skip(rangeLongValues[0]);
            inputStream.read(bytes, 0, bytes.length - 1);
            inputStream.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read bytes of resource.", e);
        }

        byte[] md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Could not calculate MD5 hash.", e);
        }

        long chunks = fileSize / bytes.length;
        long chunk = rangeLongValues[0] / bytes.length;

        return Response
                .ok(Base64.getEncoder().encodeToString(bytes))
                .tag(Base64.getEncoder().encodeToString(md5))
                .header("Content-Range",
                        RANGE_UNITS + " " + rangeLongValues[0] + "-" + rangeLongValues[1] + "/" + fileSize)
                .header("X-Chunk-Size", bytes.length)
                .header("X-Chunks", chunk + "/" + chunks)
                .build();
    }

    public long getFileSize(URL fileUrl) {
        long size = -1;
        try {
            size = Files.size(Paths.get(fileUrl.toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new InternalServerErrorException(e);
        }
        return size;
    }

}