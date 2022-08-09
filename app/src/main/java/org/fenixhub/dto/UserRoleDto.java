package org.fenixhub.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@RegisterForReflection
public class UserRoleDto {
    
    private String userId;
    private Integer roleId;

}
