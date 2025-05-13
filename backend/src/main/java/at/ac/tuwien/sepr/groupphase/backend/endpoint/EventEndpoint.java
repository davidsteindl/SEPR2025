package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreateEventDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("api/v1/events")
public class EventEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventService eventService;
    private final EventMapper eventMapper;

    @Autowired
    public EventEndpoint(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @GetMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get event by id", security = @SecurityRequirement(name = "apiKey"))
    public EventDetailDto getEventById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/events/{}", id);
        return eventMapper.eventToEventDetailDto(eventService.getEventById(id));
    }

    @GetMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all events", security = @SecurityRequirement(name = "apiKey"))
    public List<EventDetailDto> getAllEvents() {
        LOGGER.info("GET /api/v1/events");
        return eventMapper.eventsToEventDetailDtos(eventService.getAllEvents());
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new event", security = @SecurityRequirement(name = "apiKey"))
    public EventDetailDto createEvent(@RequestBody @Valid CreateEventDto createEventDto) throws ValidationException {
        LOGGER.info("POST /api/v1/events" + createEventDto);
        Event event = eventService.createEvent(eventMapper.createEventDtoToEvent(createEventDto));
        return eventMapper.eventToEventDetailDto(event);
    }
}
