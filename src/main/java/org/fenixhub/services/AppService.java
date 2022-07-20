package org.fenixhub.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.fenixhub.dto.AppChunkDto;
import org.fenixhub.dto.AppDto;
import org.fenixhub.dto.AppMetadataDto;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AppService {
    
    private static final Logger LOG = Logger.getLogger(AppService.class);

    @ConfigProperty(name = "app.range.units", defaultValue = "bytes")
    public String RANGE_UNITS;

    @ConfigProperty(name = "app.folder", defaultValue = "apps")
    private String appFolder;

    @ConfigProperty(name = "app.hash.algorithm", defaultValue = "MD5")
    private String hashAlgorithm;

    @ConfigProperty(name = "app.archive.type", defaultValue = "tar")
    private String archiveType;

    @ConfigProperty(name = "app.compression.type", defaultValue = "gzip")
    private String compressionType;


    private Path getPathOfApp(String appName) {
        Path appPath = Path.of(System.getProperty("user.home"), appFolder, appName);
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

    private long[] getRangeLongValues(String range) {
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

    private long[] getContentRangeLongValues(String range) {
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
    
    private byte[] readBytesFromFile(URL appPath, long start, long end) {
        byte[] bytes = new byte[(int) (end - start)];
        InputStream inputStream = null;
        try {
            inputStream = appPath.openStream();
            inputStream.skip(start);
            inputStream.read(bytes, (int) 0, (int) end);
            inputStream.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read bytes from file.", e);
        }
        return bytes;
    }

    private void chunkedWriteBytesToFile(Path appPath, long length, byte[] bytes, long start, long end) {
        try {
            byte[] fileContent = readBytesFromFile(appPath.toUri().toURL(), 0L, length);
            System.arraycopy(bytes, 0, fileContent, (int) start, (int) (end - start) + 1);
            OutputStream outputStream = Files.newOutputStream(appPath, StandardOpenOption.WRITE);
            outputStream.write(fileContent);
            outputStream.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not write bytes to file.", e);
        }
    }

    public AppChunkDto getAppChunk(String appName, String range) {
        long[] rangeLongValues = new long[2];
        AppMetadataDto appMetadataDto = getAppMetadata(appName);
        
        if (range != null) {
            rangeLongValues = getRangeLongValues(range);
            rangeLongValues[0] = Math.min(rangeLongValues[0], appMetadataDto.getSize());
            rangeLongValues[1] = Math.min(rangeLongValues[1], appMetadataDto.getSize());
        } else {
            rangeLongValues[0] = 0;
            rangeLongValues[1] = appMetadataDto.getSize() - 1;
        }

        byte[] bytes = new byte[(int) (rangeLongValues[1] - rangeLongValues[0] + 1)];
        try {
            bytes = readBytesFromFile(getPathOfApp(appName).toUri().toURL(), rangeLongValues[0], rangeLongValues[1] + 1);
        } catch (MalformedURLException e) {
            throw new InternalServerErrorException("Malformed file URL.", e);
        }

        String hash = getHashOfBytes(bytes);

        return AppChunkDto.builder()
        .appArchive(appMetadataDto.getArchiveName()+"."+appMetadataDto.getArchiveFormat())
        .data(bytes)
        .chunkIndexes(rangeLongValues)
        .hash(hash)
        .appSize(appMetadataDto.getSize())
        .build();
    }

    public void saveAppChunk(String appName, String archive, String contentRange, byte[] bytes) {
        if (!archive.endsWith("." + archiveType + "." + compressionType)) {
            throw new BadRequestException("Could not save app chunk. Archive type must be " + archiveType + 
            " and compression type must be " + compressionType + ".");
        }

        long[] rangeLongValues = new long[2];
        long appSize = -1;

        if (contentRange != null) {
            long[] contentRangeLongValues = getContentRangeLongValues(contentRange);
            rangeLongValues[0] = contentRangeLongValues[0];
            rangeLongValues[1] = contentRangeLongValues[1];
            appSize = contentRangeLongValues[2];
        } else {
            appSize = Base64.getDecoder().decode(bytes).length;
            rangeLongValues[0] = 0;
            rangeLongValues[1] = (int) appSize - 1;
        }

        Path appPath = getPathOfApp(archive);
        if (!Files.exists(appPath)) {
            // preallocate file size
            try {
                Files.createDirectories(appPath.getParent());
                Files.createFile(appPath);
                byte[] dummyBytes = new byte[(int) appSize];
                Files.write(appPath, dummyBytes);
            } catch (IOException e) {
                throw new InternalServerErrorException("Could not create app.", e);
            }
        }

        chunkedWriteBytesToFile(appPath, appSize, Base64.getDecoder().decode(bytes), rangeLongValues[0], rangeLongValues[1]);
    }

    public AppDto getAppInfo(String appName) {
        return AppDto.builder()
        .name(appName)
        .developer("developer")
        .build();
    }

    public AppMetadataDto getAppMetadata(String appName) {
        return AppMetadataDto.builder()
        .size(getAppSize(appName))
        .archiveName(appName.split(".")[0])
        .archiveFormat(appName.split(".")[1])
        .hash(getAppHash(appName))
        .build();
    }

}
