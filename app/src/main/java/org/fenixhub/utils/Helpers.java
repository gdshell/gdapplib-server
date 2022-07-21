package org.fenixhub.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.function.LongFunction;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public class Helpers {
    

    @ConfigProperty(name = "app.range.units", defaultValue = "bytes")
    public String RANGE_UNITS;

    @ConfigProperty(name = "app.root.folder", defaultValue = "apps")
    private String appRootFolder;
    
    @ConfigProperty(name = "app.hash.algorithm", defaultValue = "MD5")
    private String hashAlgorithm;


    public Path getPathOfApp(String appName) {
        Path appPath = Path.of(System.getProperty("user.home"), appRootFolder, appName);
        return appPath;
    }
    
    public String getHashOfBytes(byte[] bytes) {
        byte[] digest = null;
        try {
            digest = MessageDigest.getInstance(hashAlgorithm).digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Could not calculate HASH of bytes.", e);
        }
        return Base64.getEncoder().encodeToString(digest);
    }

    public long getAppSize(String appName) {
        long size = -1;
        Path appPath = getPathOfApp(appName);
        if (!Files.exists(appPath)) {
            throw new NotFoundException("App does not exist.");
        }
        try {
            size = Files.size(appPath);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read size of app.", e);
        }
        return size;
    }

    public String getAppHash(String appName) {
        String hash = null;
        try {
            InputStream is = Files.newInputStream(getPathOfApp(appName));
            hash = getHashOfBytes(is.readAllBytes());
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read file.", e);
        }
        return hash;
    }

    public long[] getRangeLongValues(String range) {
        long[] rangeLongValues = new long[2];
        String[] rangeArray = range.split("=");
        if (!RANGE_UNITS.equals(rangeArray[0])) {
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
        if (!RANGE_UNITS.equals(rangeArray[0])) {
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
    
}
