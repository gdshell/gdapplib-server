package org.fenixhub.mapper;

import org.fenixhub.dto.AppDto;
import org.fenixhub.entities.App;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AppMapper {
    AppMapper INSTANCE = Mappers.getMapper( AppMapper.class ); 
 
    AppDto appToAppDto(App app);
    App appDtoToApp(AppDto appDto);
}
