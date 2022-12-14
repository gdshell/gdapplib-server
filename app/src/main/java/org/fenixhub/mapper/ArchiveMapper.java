package org.fenixhub.mapper;

import org.fenixhub.dto.ArchiveDto;
import org.fenixhub.entity.Archive;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface ArchiveMapper {
 
    ArchiveDto archiveToArchiveDto(Archive archive);
    Archive archiveDtoToArchive(ArchiveDto archiveDto);
}
