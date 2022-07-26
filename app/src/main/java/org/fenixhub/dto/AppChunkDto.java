package org.fenixhub.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AppChunkDto extends ChunkMetadataDto {
    private int appId;
    private String appArchive;
    private byte[] data;
    private short chunksCount;
    private long appSize;

    @Builder(builderMethodName = "appChunkDtoBuilder")
    public AppChunkDto(String hash, int chunkIndex, long chunkSize, String encoding, int appId, String appArchive, byte[] data, short chunksCount, long appSize) {
        super(hash, chunkIndex, chunkSize, encoding);
        this.appId = appId;
        this.appArchive = appArchive;
        this.data = data;
        this.chunksCount = chunksCount;
        this.appSize = appSize;
    }
}