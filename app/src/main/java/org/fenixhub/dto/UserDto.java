package org.fenixhub.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.fenixhub.dto.views.UserView;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@RegisterForReflection
public class UserDto {
    
    @NotNull
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

    @NotNull
    private List<UserRoleDto> roles;
    
    public void cryptPassword() {
        password = BcryptUtil.bcryptHash(password);
    }
}
