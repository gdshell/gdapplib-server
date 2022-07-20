package org.fenixhub.dto;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDto {
    
    private Long id;
    
    @NotBlank
    private String name;

    @NotBlank
    private String developer;


    private Long lastUpdated;

}
