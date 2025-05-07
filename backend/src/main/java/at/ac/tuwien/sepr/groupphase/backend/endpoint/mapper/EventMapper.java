package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDto eventToEventDto(Event event);

    Event eventDtoToEvent(EventDto eventDto);

    List<EventDto> eventsToEventDtos(List<Event> events);

    default EventDto eventToEventDtoWithLocation(Event event) {
        EventDto eventDto = eventToEventDto(event);
        if (event.getLocation() != null) {
            eventDto.setLocationId(event.getLocation().getId());
        }
        return eventDto;
    }
}
