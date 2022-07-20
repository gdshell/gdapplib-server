package org.fenixhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppChunkDto {
    private byte[] data;
    private String hash;
    private long[] chunkIndexes;
    private long appSize;
}