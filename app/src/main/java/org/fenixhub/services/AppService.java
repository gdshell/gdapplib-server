package org.fenixhub.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.fenixhub.dto.AppChunkDto;
import org.fenixhub.dto.AppDto;
import org.fenixhub.dto.AppMetadataDto;
import org.fenixhub.entities.App;
import org.fenixhub.mapper.AppMapper;
import org.fenixhub.repository.AppRepository;
import org.fenixhub.utils.ChunkManager;
import org.fenixhub.utils.Helpers;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AppService {
    
    private static final Logger LOG = Logger.getLogger(AppService.class);

    @ConfigProperty(name = "app.archive.type", defaultValue = "tar")
    private String archiveType;

    @ConfigProperty(name = "app.compression.type", defaultValue = "br")
    public String compressionType;

    @Inject
    private ChunkManager chunkManager;
    
    @Inject
    private Helpers helpers;
    
    @Inject
    private AppRepository appRepository;

    public AppDto registerApp(AppDto appDto) {
        if (appRepository.findByName(appDto.getName()) != null) {
            throw new BadRequestException("App already exists.");
        }

        App app = App.builder()
        .name(appDto.getName())
        .developer(appDto.getDeveloper())
        .publishedAt(helpers.today.apply(0L))
        .build();
        appRepository.persist(app);

        return AppMapper.INSTANCE.appToAppDto(app);
    }

    public void saveAppChunk(String appName, String archive, String contentRange, byte[] bytes) {
        App app = appRepository.findByName(appName);
        if (app == null) {
            throw new BadRequestException("App does not exist.");
        }

        if (!archive.endsWith("." + archiveType + "." + compressionType)) {
            throw new BadRequestException("Could not save app chunk. Archive type must be " + archiveType + 
            " and compression type must be " + compressionType + ".");
        }

        long[] rangeLongValues = new long[2];
        long appSize = -1;

        if (contentRange != null) {
            long[] contentRangeLongValues = helpers.getContentRangeLongValues(contentRange);
            rangeLongValues[0] = contentRangeLongValues[0];
            rangeLongValues[1] = contentRangeLongValues[1];
            appSize = contentRangeLongValues[2];
        } else {
            appSize = Base64.getDecoder().decode(bytes).length;
            rangeLongValues[0] = 0;
            rangeLongValues[1] = (int) appSize - 1;
        }

        Path appPath = helpers.getPathOfApp(archive);
        if (!Files.exists(appPath)) {
            try {
                Files.createDirectories(appPath.getParent());
                Files.createFile(appPath);
                byte[] dummyBytes = new byte[(int) appSize];
                Files.write(appPath, dummyBytes);
            } catch (IOException e) {
                throw new InternalServerErrorException("Could not create app.", e);
            }
            app.setArchive(archive);
            appRepository.update(app);
        }

        chunkManager.chunkedWriteBytesToFile(appPath, appSize, Base64.getDecoder().decode(bytes), rangeLongValues[0], rangeLongValues[1]);
    }

    public AppChunkDto getAppChunk(String appName, String range) {
        long[] rangeLongValues = new long[2];
        AppMetadataDto appMetadataDto = getAppMetadata(appName);
        
        if (range != null) {
            rangeLongValues = helpers.getRangeLongValues(range);
            rangeLongValues[0] = Math.min(rangeLongValues[0], appMetadataDto.getSize());
            rangeLongValues[1] = Math.min(rangeLongValues[1], appMetadataDto.getSize());
        } else {
            rangeLongValues[0] = 0;
            rangeLongValues[1] = appMetadataDto.getSize() - 1;
        }

        byte[] bytes = new byte[(int) (rangeLongValues[1] - rangeLongValues[0] + 1)];
        try {
            bytes = chunkManager.chunkedReadBytesFromFile(helpers.getPathOfApp(appName).toUri().toURL(), rangeLongValues[0], rangeLongValues[1] + 1);
        } catch (MalformedURLException e) {
            throw new InternalServerErrorException("Malformed file URL.", e);
        }

        String hash = helpers.getHashOfBytes(bytes);

        return AppChunkDto.builder()
        .appArchive(appMetadataDto.getArchiveName()+"."+appMetadataDto.getArchiveFormat())
        .data(bytes)
        .chunkIndexes(rangeLongValues)
        .hash(hash)
        .appSize(appMetadataDto.getSize())
        .build();
    }



    public AppDto getAppInfo(String appName) {
        return AppDto.builder()
        .name(appName)
        .developer("developer")
        .build();
    }

    public AppMetadataDto getAppMetadata(String appName) {
        return AppMetadataDto.builder()
        .size(helpers.getAppSize(appName))
        .archiveName(appName.split(".")[0])
        .archiveFormat(appName.split(".")[1])
        .hash(helpers.getAppHash(appName))
        .build();
    }

}
