package org.fenixhub.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;

@ApplicationScoped
public class ChunkManager {

    @Inject
    private Helpers helpers;
    
    public byte[] chunkedReadBytesFromFile(URL appPath, long start, long end) {
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

    public void chunkedWriteBytesToFile(Path appChunkPath, long length, byte[] bytes, long start, long end) {
        try {
            byte[] fileContent = chunkedReadBytesFromFile(appChunkPath.toUri().toURL(), 0L, length);
            System.arraycopy(bytes, 0, fileContent, (int) start, (int) (end - start) + 1);
            OutputStream outputStream = Files.newOutputStream(appChunkPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            outputStream.write(bytes);
            outputStream.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not write bytes to file.", e);
        }
    }

    public byte[] readChunkFromFile(Path appArchivePath, int chunkIndex) {
        byte[] bytes;
        try {
            InputStream inputStream  = appArchivePath.resolve(String.valueOf(chunkIndex)).toUri().toURL().openStream();
            bytes = inputStream.readAllBytes();
            inputStream.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read bytes from file.", e);
        }
        return bytes;
    }

    public byte[] readChunkHashFromFile(Path appArchivePath, int chunkIndex) {
        byte[] bytes;
        try {
            InputStream inputStream  = appArchivePath.resolve(String.valueOf(chunkIndex) + ".sha256").toUri().toURL().openStream();
            bytes = inputStream.readAllBytes();
            inputStream.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read bytes from file.", e);
        }
        return bytes;
    }

    public void writeChunkToFile(Path chunkPath, byte[] bytes) {
        try {
            OutputStream outputStream = Files.newOutputStream(chunkPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            outputStream.write(bytes);
            outputStream.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not write bytes to file.", e);
        }
    }

    public String getBlockHash(Path archivePath, int chunks) {
        byte[] blockSha = new byte[chunks * 32];
        try {
            Files.list(archivePath).filter(chunkPath -> chunkPath.endsWith(".sha256"))
            .forEach(chunkPath -> {
                try {
                    InputStream inputStream = Files.newInputStream(chunkPath);
                    inputStream.read(blockSha, (int) (Long.parseLong(chunkPath.getFileName().toString().replace(".sha256", "")) * 32), 32);
                    inputStream.close();
                } catch (IOException e) {
                    throw new InternalServerErrorException("Could not read bytes from file.", e);
                }   
            });
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not read bytes from file.", e);
        }
        return helpers.getHashOfBytes(blockSha);
    }
    
}
