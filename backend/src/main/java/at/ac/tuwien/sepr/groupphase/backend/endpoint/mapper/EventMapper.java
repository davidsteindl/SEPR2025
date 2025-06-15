package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.CreateEventDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.UpdateEventDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "locationId", source = "location.id")
    EventDetailDto eventToEventDetailDto(Event event);

    @Mapping(target = "category", source = "category", qualifiedByName = "mapStringToEventCategory")
    @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "truncateToMinutes")
    @Mapping(target = "location", source = "locationId", qualifiedByName = "mapLocationIdToEventLocation")
    Event createEventDtoToEvent(CreateEventDto createEventDto);

    List<EventDetailDto> eventsToEventDetailDtos(List<Event> events);

    @Mapping(target = "category", source = "category", qualifiedByName = "mapStringToEventCategory")
    @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "truncateToMinutes")
    @Mapping(target = "location", source = "locationId", qualifiedByName = "mapLocationIdToEventLocation")
    Event updateEventDtoToEvent(UpdateEventDto updateEventDto);

    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "locationId", source = "location.id")
    UpdateEventDto eventToUpdateEventDto(Event event);

    @Named("mapStringToEventCategory")
    default Event.EventCategory mapStringToEventCategory(String category) {
        return Event.EventCategory.valueOf(category.toUpperCase());
    }

    @Named("mapLocationIdToEventLocation")
    default EventLocation mapLocationIdToEventLocation(Long locationId) {
        if (locationId == null) {
            return null;
        }
        EventLocation location = new EventLocation();
        location.setId(locationId);
        return location;
    }

    @Named("truncateToMinutes")
    default LocalDateTime truncateToMinutes(LocalDateTime dt) {
        return dt == null ? null : dt.truncatedTo(ChronoUnit.MINUTES);
    }
}
