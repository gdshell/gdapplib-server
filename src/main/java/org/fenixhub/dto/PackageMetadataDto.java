package org.fenixhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PackageMetadataDto {
    
    private String archiveName;
    private String archiveType;
    private String hash;
    private Long size;
}
