package org.fenixhub.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.fenixhub.dto.views.UserView;

import io.quarkus.elytron.security.common.BcryptUtil;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    
    @NotNull()
    private String id;
    
    @NotBlank(groups = UserView.Registration.class)
    private String username;
    
    @NotBlank(groups = {UserView.Registration.class, UserView.Login.class})
    private String email;

    @NotBlank(groups = {UserView.Registration.class, UserView.Login.class})
    private String password;
    
    @NotNull
    private Integer registeredAt;
    
    @NotNull
    private Boolean emailVerified;
    
    public void cryptPassword() {
        password = BcryptUtil.bcryptHash(password);
    }
}
