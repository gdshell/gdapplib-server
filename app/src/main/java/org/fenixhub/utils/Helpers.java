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
import java.util.AbstractMap.SimpleEntry;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

@ApplicationScoped
public class Helpers {

    @Inject
    private Configuration configuration;

    public Path getAppRootFolder() {
        return Path.of(System.getProperty("user.home") + "/" + configuration.getRootFolder());
    }

    public Path getPathOfApp(Integer appId) {
        Path path = getAppRootFolder().resolve(appId.toString());
        if (!Files.exists(path)) {
            throw new InternalServerErrorException("App folder does not exist.");
        }
        return path;
    }

    public Path getPathOfAppArchive(Integer appId, String archiveId) {
        Path path = getPathOfApp(appId).resolve(archiveId);
        if (!Files.exists(getAppRootFolder())) {
            throw new InternalServerErrorException("App archive folder does not exist.");
        }
        return path;
    }

    public Path getPathOfChunkByIndex(Integer appId, String archiveId, Integer index) {
        Supplier<Stream<Path>> streamPaths = () -> {
            try {
                return Files.find(
                    getPathOfAppArchive(appId, archiveId), 
                    1, 
                    (p, basicFileAttributes) ->
                    p.getFileName().toString().startsWith(index.toString() + configuration.getArchiveChunkDelimiter())
                );
            } catch (IOException e) {
                throw new InternalServerErrorException("Error while searching for chunks.");
            }
        };
        if (streamPaths.get().count() == 0) {
            throw new InternalServerErrorException("Archive chunk does not exist.");
        }
        Path path = streamPaths.get().findFirst().get();
        streamPaths.get().close();
        return path;
    }

    public Path getPathOfChunk(Integer appId, String archiveId, int chunkIndex, String chunkHash) {
        Path path = getPathOfAppArchive(appId, archiveId).resolve(Integer.toString(chunkIndex) + configuration.getArchiveChunkDelimiter() + chunkHash);
        // if (!Files.exists(path)) {
        //     throw new InternalServerErrorException("Archive chunk does not exist.");
        // }
        return path;
    }
    
    public SimpleEntry<Integer, String> getChunkNameSplit(Path chunkPath) {
        String[] split = chunkPath.getFileName().toString().split(configuration.getArchiveChunkDelimiter());
        return new SimpleEntry(Integer.parseInt(split[0]), split[1]);
    }

    public String getHashOfBytes(byte[] bytes) {
        byte[] digest = null;
        try {
            digest = MessageDigest.getInstance(configuration.getHashAlgorithm()).digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Could not calculate HASH of bytes.", e);
        }
        return new String(Base64.getUrlEncoder().encode(digest), StandardCharsets.UTF_8);
    }

    public Long getArchiveSize(Path archivePath) {
        Long size = -1L;
        if (!Files.exists(archivePath)) {
            throw new NotFoundException("App does not exist.");
        }
        try {
            size = Files.size(archivePath);
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

    public IntFunction<Integer> today = (offset) -> (int) ((System.currentTimeMillis() + offset) / 1000) ;


    public String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
}
