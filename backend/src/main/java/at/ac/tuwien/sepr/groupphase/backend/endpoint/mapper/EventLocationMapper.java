package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventLocationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventLocationMapper {
    EventLocationDto eventLocationToEventLocationDto(EventLocation eventLocation);

    EventLocation eventLocationDtoToEventLocation(EventLocationDto eventLocationDto);

    List<EventLocationDto> eventLocationsToEventLocationDtos(List<EventLocation> eventLocations);
}
