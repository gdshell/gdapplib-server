package org.fenixhub.mapper;

import org.fenixhub.dto.ArchiveDto;
import org.fenixhub.entities.Archive;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArchiveMapper {
    ArchiveMapper INSTANCE = Mappers.getMapper( ArchiveMapper.class ); 
 
    ArchiveDto archiveToArchiveDto(Archive archive);
    Archive archiveDtoToArchive(ArchiveDto archiveDto);
}
