package org.fenixhub.dto;

import javax.validation.constraints.NotNull;

import org.fenixhub.dto.views.TokenView;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@RegisterForReflection
public class TokenDto {

    private String idToken;
    private String tokenType;

    @NotNull(groups = {TokenView.Refresh.class})
    private String refreshToken;
    private Integer expiresIn;
    
}
