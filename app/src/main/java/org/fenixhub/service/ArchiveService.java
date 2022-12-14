package org.fenixhub.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
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

import org.fenixhub.dto.AppChunkDto;
import org.fenixhub.dto.ArchiveDto;
import org.fenixhub.dto.ChunkMetadataDto;
import org.fenixhub.entity.Archive;
import org.fenixhub.mapper.ArchiveMapper;
import org.fenixhub.repository.ArchiveRepository;
import org.fenixhub.utils.ChunkManager;
import org.fenixhub.utils.Configuration;
import org.fenixhub.utils.Helpers;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ArchiveService {
    
    private static final Logger LOG = Logger.getLogger(ArchiveService.class);

    @Inject Configuration configuration;
    
    @Inject Helpers helpers;

    @Inject ArchiveRepository archiveRepository;

    @Inject AppService appService;

    @Inject ChunkManager chunkManager;

    @Inject ArchiveMapper archiveMapper;

    public List<ArchiveDto> getArchives(Integer appId) {
        if (!appService.checkIfAppExists(appId)) {
            throw new NotFoundException("App not found.");
        }
        
        return archiveRepository.findByParams(Map.of("appId", appId))
        .stream().map(archiveMapper::archiveToArchiveDto)
        .collect(Collectors.toList());
    }
    
    @Transactional
    public ArchiveDto initializeAppArchive(ArchiveDto archiveDto) {
        if (!appService.checkIfAppExists(archiveDto.getAppId())) {
            throw new NotFoundException("App not found.");
        }

        if (archiveRepository.checkIfExists("app_id = :appId AND version = :version", Map.of("version", archiveDto.getVersion(), "appId", archiveDto.getAppId()))) {
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

        Archive archive = archiveMapper.archiveDtoToArchive(archiveDto);
        archive.setId(archiveId);
        archive.setCreatedAt(helpers.today.apply(0));
        archive.setUpdatedAt(helpers.today.apply(0));
        archive.setCompleted(false);
        archiveRepository.update(archive);

        return archiveMapper.archiveToArchiveDto(archive);
    }

    @Transactional
    public AppChunkDto getArchiveChunk(String archiveId, int chunkIndex) {
        Archive archive = archiveRepository.findById(archiveId);
        if (archive == null) {
            throw new NotFoundException("Archive not found.");
        }

        ArchiveDto archiveDto = archiveMapper.archiveToArchiveDto(archive);
        
        Path chunkPath = helpers.getPathOfChunkByIndex(archiveDto.getAppId(), archiveId, chunkIndex);
        byte[] chunkBytes;
        try {
            chunkBytes = Files.readAllBytes(chunkPath);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not copy chunk.", e);
        }
        SimpleEntry<Integer, String> chunkMeta = helpers.getChunkNameSplit(chunkPath);
        
        return AppChunkDto.appChunkDtoBuilder()
        .appId(archiveDto.getAppId())
        .appArchive(archiveDto.getArchive())
        .appSize(archiveDto.getSize())
        .chunksCount(archiveDto.getChunks())
        .chunkIndex(chunkMeta.getKey())
        .chunkSize(chunkBytes.length)
        .data(Base64.getEncoder().encode(chunkBytes))
        .encoding(configuration.getCompressionType())
        .hash(chunkMeta.getValue())
        .build();
    }

    @Transactional
    public List<ChunkMetadataDto> getArchiveChunks(String archiveId) {
        Archive archive = archiveRepository.findById(archiveId);
        if (archive == null) {
            throw new NotFoundException("Archive not found.");
        }
        
        List<ChunkMetadataDto> chunks = new ArrayList<ChunkMetadataDto>();

        ArchiveDto archiveDto = archiveMapper.archiveToArchiveDto(archive);
        try {
            Files.list(helpers.getPathOfAppArchive(archiveDto.getAppId(), archiveDto.getId().toString())).forEach(chunkPath -> {
                try {
                    byte[] chunkBytes = Files.readAllBytes(chunkPath);
                    SimpleEntry<Integer, String> chunkMeta = helpers.getChunkNameSplit(chunkPath);
                    chunks.add(
                        ChunkMetadataDto.builder()
                        .chunkIndex(chunkMeta.getKey())
                        .chunkSize(chunkBytes.length)
                        .encoding(configuration.getCompressionType())
                        .hash(chunkMeta.getValue())
                        .build()
                    );
                } catch (IOException e) {
                    throw new InternalServerErrorException("Could not copy chunks.", e);
                }
            });
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not copy chunks.", e);
        }

        return chunks;
    }

    /*
     * Upload an app archive in chunks (or full).
     * This method will store the app archive in the filesystem.
     */
    @Transactional
    public void saveAppChunk(String archiveId, int chunkSize, int chunkIndex, String chunkHash, boolean checkIntegrity, byte[] base64bytes) {
        Archive archive = archiveRepository.findById(archiveId);
        if (archive == null) {
            throw new NotFoundException("Archive not found.");
        }

        ArchiveDto archiveDto = archiveMapper.archiveToArchiveDto(archive);

        if (chunkIndex > archiveDto.getChunks()) {
            throw new BadRequestException("Chunk index can't be more than chunks count.");
        }
        
        byte[] decodedBytes = null;
        try {
            decodedBytes = Base64.getDecoder().decode(base64bytes);
        } catch(IllegalArgumentException e) {
            throw new BadRequestException("Could not save app chunk. Base64 decoding failed.");
        }

        if (decodedBytes.length != chunkSize) {
            throw new BadRequestException("Could not save app chunk. Declared length and actual length do not match.");
        }
        
        if (!helpers.getHashOfBytes(decodedBytes).equals(chunkHash)) {
            throw new BadRequestException("Could not save app chunk. Hash does not match.");
        }

        Path chunkPath = helpers.getPathOfChunk(archiveDto.getAppId(), archiveId, chunkIndex, chunkHash);
        if (
            (Files.exists(chunkPath) && !chunkManager.readChunkHashFromFile(helpers.getPathOfAppArchive(archiveDto.getAppId(), archiveId), chunkIndex).equals(chunkHash)) ||
            (!Files.exists(chunkPath))
        ) {
            chunkManager.writeChunkToFile(chunkPath, decodedBytes);
        }

        // Check integrity of block
        if (checkIntegrity && chunkIndex == archiveDto.getChunks()) {
            if (!chunkManager.getBlockHash(helpers.getPathOfAppArchive(archiveDto.getAppId(), archiveId), chunkSize).equals(archiveDto.getHash())) {
                throw new BadRequestException("Could not save app chunk. Block hash does not match.");
            }
            archiveRepository.setCompleted(archive, true);
        }

        LOG.infof("Chunk %s saved for app %s at path %s", chunkIndex, archiveDto.getAppId(), chunkPath);
    }


    /*
     * 
     */
    @Transactional
    public void saveAppChunk(String archiveId, int chunkSize, int chunkIndex, String chunkHash, boolean checkIntegrity, String base64data) {
        saveAppChunk(archiveId, chunkSize, chunkIndex, chunkHash, checkIntegrity, base64data.getBytes());
    }

}
