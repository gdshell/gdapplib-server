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
    private String developer;

    @NotNull
    private Long registeredAt;

    @NotNull
    private Long updatedAt;

    @NotNull
    private Boolean published;

}
