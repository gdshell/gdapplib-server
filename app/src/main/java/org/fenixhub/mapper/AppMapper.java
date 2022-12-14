package org.fenixhub.mapper;

import org.fenixhub.dto.AppDto;
import org.fenixhub.entity.App;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface AppMapper {
 
    AppDto appToAppDto(App app);
    App appDtoToApp(AppDto appDto);
    List<AppDto> appsToAppDtos(List<App> apps);
}
