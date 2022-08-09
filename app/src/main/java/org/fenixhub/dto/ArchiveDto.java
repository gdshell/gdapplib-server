package org.fenixhub.dto;

import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.fenixhub.dto.views.ArchiveView;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@RegisterForReflection
public class ArchiveDto {
    

    @NotNull
    private UUID id;

    @NotNull(groups = {ArchiveView.Initialize.class})
    private Integer appId;
    
    @NotBlank(groups = {ArchiveView.Initialize.class})
    private String archive;

    @NotEmpty(groups = {ArchiveView.Initialize.class})
    private String hash;
    
    @NotNull(groups = {ArchiveView.Initialize.class})
    private Long size;

    @NotNull(groups = {ArchiveView.Initialize.class})
    private Short chunks;

    @NotNull(groups = {ArchiveView.Initialize.class})
    private String version;

    @NotNull
    private Boolean completed;
    
}
