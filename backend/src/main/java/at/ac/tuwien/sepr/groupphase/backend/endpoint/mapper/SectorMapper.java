package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StageSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.StageSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SectorMapper {

    default SectorDto entityToDto(Sector sector) throws ValidationException {
        if (sector instanceof StandingSector s) {
            return standingToDto(s);
        }
        if (sector instanceof StageSector s) {
            return stageToDto(s);
        }
        throw new ValidationException("Unknown sector type: " + sector.getClass(), List.of("Sector type must be either StandingSector or StageSector"));
    }

    default Sector dtoToEntity(SectorDto dto) throws ValidationException {
        if (dto instanceof StandingSectorDto s) {
            return standingToEntity(s);
        }
        if (dto instanceof StageSectorDto s) {
            return stageToEntity(s);
        }
        throw new ValidationException("Unknown sector DTO type: " + dto.getClass(),
            List.of("Sector DTO type must be either StandingSectorDto or StageSectorDto"));
    }

    default List<SectorDto> entitiesToDtos(List<Sector> sectors) throws ValidationException {
        if (sectors == null) {
            return null;
        }
        List<SectorDto> result = new java.util.ArrayList<>();
        for (Sector s : sectors) {
            result.add(entityToDto(s));
        }
        return result;
    }

    default List<Sector> dtosToEntities(List<SectorDto> dtos) throws ValidationException {
        if (dtos == null) {
            return null;
        }
        List<Sector> result = new java.util.ArrayList<>();
        for (SectorDto d : dtos) {
            result.add(dtoToEntity(d));
        }
        return result;
    }

    StandingSectorDto standingToDto(StandingSector standing);

    @Mapping(target = "room", ignore = true)
    StandingSector standingToEntity(StandingSectorDto dto);

    StageSectorDto stageToDto(StageSector stage);

    @Mapping(target = "room", ignore = true)
    StageSector stageToEntity(StageSectorDto dto);
}

