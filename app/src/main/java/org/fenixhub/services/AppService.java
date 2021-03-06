package org.fenixhub.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

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


    /*
     * Create a new app.
     * This method will create a new app and store it's root folder in the filesystem.
     * The root folder will be later used to store the app's archive.
     */
    public AppDto registerApp(AppDto appDto) {
        if (appRepository.findByName(appDto.getName()) != null) {
            throw new BadRequestException("App already exists.");
        }

        App app = App.builder()
        .name(appDto.getName())
        .developer(appDto.getDeveloper())
        .registeredAt(helpers.today.apply(0L))
        .published(false)
        .build();
        appRepository.persist(app);

        try {
            Files.createDirectories(helpers.getPathOfApp(app.getId()));
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not create app folder.", e);
        }

        return AppMapper.INSTANCE.appToAppDto(app);
    }

    public void updateApp(Long appId, AppDto appDto) {
        App app = appRepository.findById(appId);
        if (app == null) {
            throw new NotFoundException("App does not exist.");
        }

        if (appDto.getName() != null) {
            app.setName(appDto.getName());
        }
        if (appDto.getPublished() != null) {
            app.setPublished(appDto.getPublished());
        }

        app.setUpdatedAt(helpers.today.apply(0L));
        
        appRepository.update(app);
    }

    /*
     * Upload an app archive in chunks (or full).
     * This method will store the app archive in the filesystem.
     */
    public void saveAppChunk(Long appId, String archive, String contentRange, byte[] bytes) {
        App app = appRepository.findById(appId);
        if (app == null) {
            throw new NotFoundException("App does not exist.");
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

        Path appPath = helpers.getPathOfAppArchive(appId, archive);
        if (!Files.exists(appPath)) {
            try {
                Files.createFile(appPath);
                byte[] dummyBytes = new byte[(int) appSize];
                Files.write(appPath, dummyBytes);
            } catch (IOException e) {
                throw new InternalServerErrorException("Could not create app archive.", e);
            }
            app.setArchive(archive);
            appRepository.update(app);
        }

        chunkManager.chunkedWriteBytesToFile(appPath, appSize, Base64.getDecoder().decode(bytes), rangeLongValues[0], rangeLongValues[1]);
    }

    /*
     * Get a chunk of the app archive.
     * 
     */
    public AppChunkDto getAppChunk(Long appId, String range) {
        App app = appRepository.findById(appId);
        if (app == null) {
            throw new NotFoundException("App does not exist.");
        }
        
        long[] rangeLongValues = new long[2];
        AppMetadataDto appMetadataDto = getAppMetadata(appId);
        
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
            bytes = chunkManager.chunkedReadBytesFromFile(helpers.getPathOfAppArchive(appId, appMetadataDto.getArchive()).toUri().toURL(), rangeLongValues[0], rangeLongValues[1] + 1);
        } catch (MalformedURLException e) {
            throw new InternalServerErrorException("Malformed file URL.", e);
        }

        String hash = helpers.getHashOfBytes(bytes);

        return AppChunkDto.builder()
        .appArchive(appMetadataDto.getArchive())
        .data(bytes)
        .chunkIndexes(rangeLongValues)
        .hash(hash)
        .appSize(appMetadataDto.getSize())
        .build();
    }

    public AppDto getAppInfo(Long appId) {
        App app = appRepository.findById(appId);
        if (app == null) {
            throw new NotFoundException("App does not exist.");
        }

        return AppMapper.INSTANCE.appToAppDto(app);
    }

    public AppMetadataDto getAppMetadata(Long appId) {
        App app = appRepository.findById(appId);
        if (app == null) {
            throw new NotFoundException("App does not exist.");
        }

        Path appPath = helpers.getPathOfAppArchive(appId, app.getArchive());
        long appSize = helpers.getAppSize(appPath);
        String hash = helpers.getAppHash(appPath);

        return AppMetadataDto.builder()
        .appId(appId)
        .archive(app.getArchive())
        .hash(hash)
        .size(appSize)
        .build();
    }

}
