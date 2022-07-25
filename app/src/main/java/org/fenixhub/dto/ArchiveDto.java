package org.fenixhub.dto;

import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import io.smallrye.common.constraint.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// TODO: add profiles in order to validate request/responses

@Data
@Builder
@AllArgsConstructor
public class ArchiveDto {
    

    @NotNull
    private UUID id;

    @NotNull
    private Long appId;
    
    @NotBlank
    private String archive;

    @NotEmpty
    private String hash;
    
    @NotNull
    private Long size;

    @NotNull
    private Long chunks;

    @NotNull
    private String version;
    
}
