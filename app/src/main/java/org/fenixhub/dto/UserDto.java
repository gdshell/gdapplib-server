package org.fenixhub.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import org.fenixhub.dto.views.UserView;
import org.mindrot.jbcrypt.BCrypt;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@Builder
@RegisterForReflection
public class UserDto {
    
    @NotNull(groups = { UserView.Public.class })
    private String id;
    
    @NotBlank(groups = {UserView.Registration.class, UserView.Public.class})
    private String username;
    
    @NotBlank(groups = {UserView.Registration.class, UserView.Login.class, UserView.Public.class})
    private String email;

    @NotBlank(groups = {UserView.Registration.class, UserView.Login.class})
    private String password;
    
    @NotNull
    private Integer registeredAt;
    
    @NotNull
    private Boolean emailVerified;

    @NotNull
    private Set<ERole> roles;
    
    public String cryptPassword() {
        password = BCrypt.hashpw(password, BCrypt.gensalt());
        return password;
    }
}
