package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StageSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.StageSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SectorMapper {

    default SectorDto entityToDto(Sector sector) throws ValidationException {
        if (sector == null) {
            return null;
        }
        if (sector instanceof StandingSector s) {
            return standingToDto(s);
        }
        if (sector instanceof StageSector s) {
            return stageToDto(s);
        }
        if (sector.getClass() == Sector.class) {
            SectorDto dto = new SectorDto();
            dto.setId(sector.getId());
            dto.setPrice(sector.getPrice());
            dto.setType(SectorType.NORMAL);
            return dto;
        }
        throw new ValidationException(
            "Unknown sector type: " + sector.getClass(),
            List.of("Sector type must be StandingSector, StageSector or normal Sector")
        );
    }

    default Sector dtoToEntity(SectorDto dto) throws ValidationException {
        if (dto == null) {
            return null;
        }
        if (dto instanceof StandingSectorDto s) {
            return standingToEntity(s);
        }
        if (dto instanceof StageSectorDto s) {
            return stageToEntity(s);
        }
        if (dto.getType() == SectorType.NORMAL) {
            Sector s = new Sector();
            s.setId(dto.getId());
            s.setPrice(dto.getPrice());
            return s;
        }
        throw new ValidationException(
            "Unknown sector DTO type: " + dto.getClass(),
            List.of("Sector DTO type must be StandingSectorDto, StageSectorDto, or type NORMAL")
        );
    }

    default List<SectorDto> entitiesToDtos(List<Sector> sectors) throws ValidationException {
        if (sectors == null) {
            return null;
        }
        List<SectorDto> result = new ArrayList<>();
        for (Sector s : sectors) {
            result.add(entityToDto(s));
        }
        return result;
    }

    default List<Sector> dtosToEntities(List<SectorDto> dtos) throws ValidationException {
        if (dtos == null) {
            return null;
        }
        List<Sector> result = new ArrayList<>();
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

