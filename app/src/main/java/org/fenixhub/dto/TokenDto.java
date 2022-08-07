package org.fenixhub.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenDto {

    private String idToken;
    private String tokenType;
    private String refreshToken;
    private Integer expiresIn;
    
}
