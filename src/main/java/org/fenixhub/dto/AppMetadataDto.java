package org.fenixhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppMetadataDto {
    
    private Long appId;
    private String archiveName;
    private String archiveFormat;
    private String hash;
    private Long size;
    
}
