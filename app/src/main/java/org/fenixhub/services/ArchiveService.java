package org.fenixhub.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

import org.fenixhub.dto.ArchiveDto;
import org.fenixhub.entities.Archive;
import org.fenixhub.mapper.ArchiveMapper;
import org.fenixhub.repository.ArchiveRepository;
import org.fenixhub.utils.ChunkManager;
import org.fenixhub.utils.Configuration;
import org.fenixhub.utils.Helpers;

@ApplicationScoped
public class ArchiveService {
    
    @Inject
    private Configuration configuration;
    
    @Inject
    private Helpers helpers;

    @Inject
    private ArchiveRepository archiveRepository;

    @Inject
    private AppService appService;

    @Inject
    private ChunkManager chunkManager;

    public List<ArchiveDto> getArchivesByAppId(Long appId) {
        if (!appService.checkIfAppExists(appId)) {
            throw new NotFoundException("App not found.");
        }
        
        return archiveRepository.findByAppId(appId).stream().map(ArchiveMapper.INSTANCE::archiveToArchiveDto).collect(Collectors.toList());
    }
    
    @Transactional
    public ArchiveDto initializeAppArchive(ArchiveDto archiveDto) {
        if (!appService.checkIfAppExists(archiveDto.getAppId())) {
            throw new NotFoundException("App not found.");
        }

        if (archiveRepository.checkIfExists("version = :version", Map.of("version", archiveDto.getVersion()))) {
            throw new BadRequestException("Archive with such version already exists.");
        }

        if (!archiveDto.getArchive().endsWith("." + configuration.getArchiveType()  + "." + configuration.getCompressionType())) {
            throw new BadRequestException("Archive type must be " + configuration.getArchiveType() + 
            " and compression type must be " + configuration.getCompressionType() + ".");
        }

        String archiveId = helpers.generateUUID();
        Path archivePath = helpers.getPathOfAppArchive(archiveDto.getAppId(), archiveId);
        try {
            Files.createDirectories(archivePath);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not create app folder.", e);
        }

        Archive archive = ArchiveMapper.INSTANCE.archiveDtoToArchive(archiveDto);
        archive.setId(archiveId);
        archive.setCreatedAt(helpers.today.apply(0L));
        archive.setUpdatedAt(helpers.today.apply(0L));
        archiveRepository.update(archive);

        return ArchiveMapper.INSTANCE.archiveToArchiveDto(archive);
    }

    /*
     * Upload an app archive in chunks (or full).
     * This method will store the app archive in the filesystem.
     */
    @Transactional
    public void saveAppChunk(String archiveId, int chunkSize, int chunkIndex, String chunkHash, boolean checkIntegrity, byte[] bytes) {
        Archive archive = archiveRepository.findById(archiveId);
        if (archive == null) {
            throw new NotFoundException("Archive not found.");
        }

        ArchiveDto archiveDto = ArchiveMapper.INSTANCE.archiveToArchiveDto(archive);

        if (chunkIndex > archiveDto.getChunks()) {
            throw new BadRequestException("Chunk index can't be more than chunks count.");
        }
        
        byte[] decodedBytes = null;
        try {
            decodedBytes = Base64.getDecoder().decode(bytes);
        } catch(IllegalArgumentException e) {
            throw new BadRequestException("Could not save app chunk. Base64 decoding failed.");
        }

        if (decodedBytes.length != chunkSize) {
            throw new BadRequestException("Could not save app chunk. Declared length and actual length do not match.");
        }
        

        if (!helpers.getHashOfBytes(decodedBytes).equals(chunkHash)) {
            throw new BadRequestException("Could not save app chunk. Hash does not match.");
        }

        Path chunkPath = helpers.getPathOfChunk(archiveDto.getAppId(), archiveId, chunkIndex);
        Path chunkHashPath = helpers.getPathOfChunkHash(archiveDto.getAppId(), archiveId, chunkIndex);
        if (
            (Files.exists(chunkPath) && !chunkManager.readChunkHashFromFile(helpers.getPathOfAppArchive(archiveDto.getAppId(), archiveId), chunkIndex).equals(chunkHash)) ||
            (!Files.exists(chunkPath))
        ) {
            chunkManager.writeChunkToFile(chunkPath, decodedBytes);
            chunkManager.writeChunkToFile(chunkHashPath, chunkHash.getBytes());
        }

        // Check integrity of block
        if (checkIntegrity && chunkIndex == archiveDto.getChunks()) {
            if (!chunkManager.getBlockHash(helpers.getPathOfAppArchive(archiveDto.getAppId(), archiveId), chunkSize).equals(archiveDto.getHash())) {
                throw new BadRequestException("Could not save app chunk. Block hash does not match.");
            }
        }

    }

    // /*
    //  * Get a chunk of the app archive.
    //  * 
    //  */
    // public AppChunkDto getAppChunk(Long appId, Long chunkIndex) {
    //     App app = appRepository.findById(appId);
    //     if (app == null) {
    //         throw new NotFoundException("App does not exist.");
    //     }

    //     Path appPath = helpers.getPathOfApp(appId);
    //     if (!Files.exists(appPath)) {
    //         throw new NotFoundException("App does not exist.");
    //     }
        
    //     AppMetadataDto appMetadataDto = getAppMetadata(appId);

    //     byte[] bytes = chunkManager.readChunkFromFile(helpers.getPathOfApp(appId), chunkIndex);

    //     String hash = helpers.getHashOfBytes(bytes);

    //     return AppChunkDto.builder()
    //     .appArchive(appMetadataDto.getArchive())
    //     .data(bytes)
    //     .chunkIndex(chunkIndex)
    //     .chunkSize(bytes.length)
    //     .chunksCount(appMetadataDto.getChunksCount())
    //     .hash(hash)
    //     // .appSize(appMetadataDto.getSize())
    //     .build();
    // }

    // public AppDto getAppInfo(Long appId) {
    //     App app = appRepository.findById(appId);
    //     if (app == null) {
    //         throw new NotFoundException("App does not exist.");
    //     }

    //     return AppMapper.INSTANCE.appToAppDto(app);
    // }

    // public AppMetadataDto getAppMetadata(Long appId) {
    //     App app = appRepository.findById(appId);
    //     if (app == null) {
    //         throw new NotFoundException("App does not exist.");
    //     }

    //     Path appPath = helpers.getPathOfApp(appId);
    //     // long appSize = helpers.getAppSize(appPath);
    //     // String hash = helpers.getAppHash(appPath);
    //     long chunksCount = -1;

    //     try {
    //         chunksCount = Files.list(appPath).count();
    //     } catch (IOException e) {
    //         throw new InternalServerErrorException("Could not count chunks.");
    //     }

    //     return AppMetadataDto.builder()
    //     .appId(appId)
    //     .archive(app.getArchive())
    //     .chunksCount(chunksCount)
    //     // .hash(hash)
    //     // .size(appSize)
    //     .build();
    // }



}
