package org.fenixhub.mapper;

import org.fenixhub.dto.AppDto;
import org.fenixhub.entities.App;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface AppMapper {
 
    AppDto appToAppDto(App app);
    App appDtoToApp(AppDto appDto);
}
