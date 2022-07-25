package org.fenixhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppChunkDto {
    private long appId;
    private String appArchive;
    private byte[] data;
    private String hash;
    private long chunkIndex;
    private long chunkSize;
    private long chunksCount;
    private long appSize;
}