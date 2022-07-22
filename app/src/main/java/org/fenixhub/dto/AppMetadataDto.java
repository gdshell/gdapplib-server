package org.fenixhub.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import io.smallrye.common.constraint.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AppMetadataDto {
    
    @NotNull
    private Long appId;
    
    @NotBlank
    private String archive;

    @NotEmpty
    private String hash;
    
    @NotNull
    private Long size;
    
}
