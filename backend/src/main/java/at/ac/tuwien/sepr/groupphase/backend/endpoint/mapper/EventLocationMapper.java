package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.CreateEventLocationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventLocationMapper {

    EventLocationDetailDto eventLocationToEventLocationDetailDto(EventLocation eventLocation);

    @Mapping(target = "type", source = "type", qualifiedByName = "mapStringToLocationType")
    EventLocation createEventLocationDtoToEventLocation(CreateEventLocationDto createEventLocationDto);

    List<EventLocationDetailDto> eventLocationsToEventLocationDtos(List<EventLocation> eventLocations);

    @Named("mapStringToLocationType")
    default EventLocation.LocationType mapStringToLocationType(String type) {
        return EventLocation.LocationType.valueOf(type.toUpperCase());
    }
}
