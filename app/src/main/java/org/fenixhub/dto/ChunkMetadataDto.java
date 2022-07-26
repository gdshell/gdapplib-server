package org.fenixhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkMetadataDto {
    private String hash;
    private int chunkIndex;
    private long chunkSize;
    private String encoding;
}
