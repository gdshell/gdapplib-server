package org.fenixhub.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDto {
    
    @NotNull
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String archive;

    @NotBlank
    private String developer;

    @NotNull
    private Long publishedAt;

    @NotNull
    private Long updatedAt;

    @NotNull
    private AppMetadataDto metadata;

}
