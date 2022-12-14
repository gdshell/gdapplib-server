package org.fenixhub.mapper;

import org.fenixhub.dto.ERole;
import org.fenixhub.dto.UserDto;
import org.fenixhub.entity.Role;
import org.fenixhub.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface UserMapper {

    UserDto userToUserDto(User user);
    User userDtoToUser(UserDto userDto);
    default ERole roleToRoleName(Role role) {
        return ERole.valueOf(role.getName());
    };
}
