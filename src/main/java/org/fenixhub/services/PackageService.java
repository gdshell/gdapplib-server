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
import org.fenixhub.dto.PackageChunkDto;
import org.fenixhub.dto.PackageInfoDto;
import org.fenixhub.dto.PackageMetadataDto;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PackageService {
    
    private static final Logger LOG = Logger.getLogger(PackageService.class);

    @ConfigProperty(name = "package.range.units", defaultValue = "bytes")
    private String RANGE_UNITS;

    @ConfigProperty(name = "package.folder", defaultValue = "packages")
    private String packageFolder;

    @ConfigProperty(name = "package.hash.algorithm", defaultValue = "MD5")
    private String hashAlgorithm;

    @ConfigProperty(name = "package.archive.type", defaultValue = "tar.gz")
    private String archiveType;


    private Path getPathOfPackage(String packageName) {
        Path packagePath = Path.of(System.getProperty("user.home"), packageFolder, packageName);
        return packagePath;
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

    public long getPackageSize(String packageName) {
        long size = -1;
        Path packagePath = getPathOfPackage(packageName);
        if (!Files.exists(packagePath)) {
            throw new NotFoundException("Package does not exist.");
        }
        try {
            size = Files.size(packagePath);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read size of package.", e);
        }
        return size;
    }

    public String getPackageHash(String packageName) {
        String hash = null;
        try {
            InputStream is = Files.newInputStream(getPathOfPackage(packageName));
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
    
    private byte[] readBytesFromFile(URL packagePath, long start, long end) {
        byte[] bytes = new byte[(int) (end - start)];
        InputStream inputStream = null;
        try {
            inputStream = packagePath.openStream();
            inputStream.skip(start);
            inputStream.read(bytes, (int) 0, (int) end);
            inputStream.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read bytes from file.", e);
        }
        return bytes;
    }

    private void chunkedWriteBytesToFile(Path packagePath, long length, byte[] bytes, long start, long end) {
        try {
            byte[] fileContent = readBytesFromFile(packagePath.toUri().toURL(), 0L, length);
            System.arraycopy(bytes, 0, fileContent, (int) start, (int) (end - start) + 1);
            OutputStream outputStream = Files.newOutputStream(packagePath, StandardOpenOption.WRITE);
            outputStream.write(fileContent);
            outputStream.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not write bytes to file.", e);
        }
    }

    public PackageChunkDto getPackageChunk(String packageName, String range) {
        long[] rangeLongValues = new long[2];
        long packageSize = getPackageSize(packageName);
        
        if (range != null) {
            rangeLongValues = getRangeLongValues(range);
            rangeLongValues[0] = Math.min(rangeLongValues[0], packageSize);
            rangeLongValues[1] = Math.min(rangeLongValues[1], packageSize);
        } else {
            rangeLongValues[0] = 0;
            rangeLongValues[1] = (int) packageSize - 1;
        }

        byte[] bytes = new byte[(int) (rangeLongValues[1] - rangeLongValues[0] + 1)];
        try {
            bytes = readBytesFromFile(getPathOfPackage(packageName).toUri().toURL(), rangeLongValues[0], rangeLongValues[1] + 1);
        } catch (MalformedURLException e) {
            throw new InternalServerErrorException("Malformed file URL.", e);
        }

        String hash = getHashOfBytes(bytes);

        return PackageChunkDto.builder()
        .data(bytes)
        .chunkIndexes(rangeLongValues)
        .hash(hash)
        .packageSize(packageSize)
        .build();
    }

    public void savePackageChunk(String packageName, String archive, String contentRange, byte[] bytes) {
        if (!archive.endsWith("." + archiveType)) {
            throw new BadRequestException("Could not save package chunk. Archive type must be " + archiveType + ".");
        }

        long[] rangeLongValues = new long[2];
        long packageSize = -1;

        if (contentRange != null) {
            long[] contentRangeLongValues = getContentRangeLongValues(contentRange);
            rangeLongValues[0] = contentRangeLongValues[0];
            rangeLongValues[1] = contentRangeLongValues[1];
            packageSize = contentRangeLongValues[2];
        } else {
            packageSize = Base64.getDecoder().decode(bytes).length;
            rangeLongValues[0] = 0;
            rangeLongValues[1] = (int) packageSize - 1;
        }

        Path packagePath = getPathOfPackage(archive);
        if (!Files.exists(packagePath)) {
            // preallocate file size
            try {
                Files.createDirectories(packagePath.getParent());
                Files.createFile(packagePath);
                byte[] dummyBytes = new byte[(int) packageSize];
                Files.write(packagePath, dummyBytes);
            } catch (IOException e) {
                throw new InternalServerErrorException("Could not create package.", e);
            }
        }

        chunkedWriteBytesToFile(packagePath, packageSize, Base64.getDecoder().decode(bytes), rangeLongValues[0], rangeLongValues[1]);
    }

    public PackageInfoDto getPackageInfo(String packageName) {
        return PackageInfoDto.builder()
        .name(packageName)
        .developer("developer")
        .build();
    }

    public PackageMetadataDto getPackageMetadata(String packageName) {
        return PackageMetadataDto.builder()
        .size(getPackageSize(packageName))
        .archiveName(packageName.split(".")[0])
        .archiveType(packageName.split(".")[1])
        .hash(getPackageHash(packageName))
        .build();
    }

}
