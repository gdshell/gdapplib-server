package org.fenixhub.mapper;

import org.fenixhub.dto.UserDto;
import org.fenixhub.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper( UserMapper.class ); 
 
    UserDto userToUserDto(User user);
    User userDtoToUser(UserDto userDto);
}
