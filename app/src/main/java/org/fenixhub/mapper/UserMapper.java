package org.fenixhub.mapper;

import java.util.List;

import org.fenixhub.dto.UserDto;
import org.fenixhub.dto.UserRoleDto;
import org.fenixhub.entities.User;
import org.fenixhub.entities.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface UserMapper {
 
    UserDto userToUserDto(User user);
    User userDtoToUser(UserDto userDto);

    List<UserRoleDto> userRoleToUserRoleDto(List<UserRole> userRole);
    List<UserRole> userRoleDtoToUserRole(List<UserRoleDto> userRoleDto);

}
