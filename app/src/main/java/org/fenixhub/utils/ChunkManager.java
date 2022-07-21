package org.fenixhub.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.InternalServerErrorException;

@ApplicationScoped
public class ChunkManager {
    
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

    public void chunkedWriteBytesToFile(Path appPath, long length, byte[] bytes, long start, long end) {
        try {
            byte[] fileContent = chunkedReadBytesFromFile(appPath.toUri().toURL(), 0L, length);
            System.arraycopy(bytes, 0, fileContent, (int) start, (int) (end - start) + 1);
            OutputStream outputStream = Files.newOutputStream(appPath, StandardOpenOption.WRITE);
            outputStream.write(fileContent);
            outputStream.close();
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not write bytes to file.", e);
        }
    }
    
}
