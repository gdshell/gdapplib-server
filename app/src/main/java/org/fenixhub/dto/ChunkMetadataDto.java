package org.fenixhub.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@RegisterForReflection
public class ChunkMetadataDto {
    private String hash;
    private int chunkIndex;
    private long chunkSize;
    private String encoding;
}
