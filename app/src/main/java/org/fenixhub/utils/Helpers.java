package org.fenixhub.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.function.LongFunction;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;

@ApplicationScoped
public class Helpers {

    @Inject
    private Configuration configuration;

    public Path getAppRootFolder() {
        return Path.of(System.getProperty("user.home") + "/" + configuration.getRootFolder());
    }

    public Path getPathOfApp(Long appId) {
        return getAppRootFolder().resolve(appId.toString());
    }

    public Path getPathOfAppArchive(Long appId, String archiveId) {
        return getPathOfApp(appId).resolve(archiveId);
    }

    public Path getPathOfChunk(Long appId, String archiveId, int chunkIndex) {
        return getPathOfAppArchive(appId, archiveId).resolve(Integer.toString(chunkIndex));
    }

    public Path getPathOfChunkHash(Long appId, String archiveId, int chunkIndex) {
        return getPathOfAppArchive(appId, archiveId).resolve(Integer.toString(chunkIndex) + ".sha256");
    }

    public String getHashOfAppChunk(Long appId, String archiveId, int chunkIndex) {
        return getHashOfFile(getPathOfChunk(appId, archiveId, chunkIndex));
    }
    
    public String getHashOfAppChunk(Path appChunkPath) {
        // if (!Files.exists(appChunkPath)) {
        //     throw new InternalServerErrorException("App chunk does not exist.");
        // }
        return getHashOfFile(appChunkPath);
    }
    
    public String getHashOfBytes(byte[] bytes) {
        byte[] digest = null;
        try {
            digest = MessageDigest.getInstance(configuration.getHashAlgorithm()).digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Could not calculate HASH of bytes.", e);
        }
        return new String(Base64.getEncoder().encode(digest), StandardCharsets.UTF_8);
    }

    public long getAppSize(Path appArchivePath) {
        long size = -1;
        if (!Files.exists(appArchivePath)) {
            throw new NotFoundException("App does not exist.");
        }
        try {
            size = Files.size(appArchivePath);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read size of app.", e);
        }
        return size;    
    }

    public String getHashOfFile(Path filePath) {
        String hash = null;
        try {
            InputStream is = Files.newInputStream(filePath);
            hash = getHashOfBytes(is.readAllBytes());
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read file.", e);
        }
        return hash;
    }

    public long[] getRangeLongValues(String range) {
        long[] rangeLongValues = new long[2];
        String[] rangeArray = range.split("=");
        if (!configuration.getRangeUnits().equals(rangeArray[0])) {
            throw new WebApplicationException("Server unable to handle " + rangeArray[0] + " range units.", 416);
        }
        String[] rangeValues = rangeArray[1].split("-");
        rangeLongValues[0] = Long.parseLong(rangeValues[0]);
        rangeLongValues[1] = Long.parseLong(rangeValues[1]);
        return rangeLongValues;
    }

    public long[] getContentRangeLongValues(String range) {
        long[] contentRangeLongValues = new long[3];
        String[] rangeArray = range.split(" ");
        if (!configuration.getRangeUnits().equals(rangeArray[0])) {
            throw new WebApplicationException("Server unable to handle " + rangeArray[0] + " range units.", 416);
        }
        String[] contentRange = rangeArray[1].split("/");
        String[] rangeValues = contentRange[0].split("-");
        contentRangeLongValues[0] = Long.parseLong(rangeValues[0]);
        contentRangeLongValues[1] = Long.parseLong(rangeValues[1]);
        contentRangeLongValues[2] = Long.parseLong(contentRange[1]);
        return contentRangeLongValues;
    }


    public LongFunction<Long> today = (offset) -> (System.currentTimeMillis() + offset) / 1000L ;


    public String generateUUID() {
        return UUID.randomUUID().toString();
    }



    
}
