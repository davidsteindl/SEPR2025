package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.CreateEventLocationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventLocationMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.service.EventLocationService;
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
@RequestMapping("api/v1/locations")
public class EventLocationEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventLocationService eventLocationService;
    private final EventLocationMapper eventLocationMapper;

    @Autowired
    public EventLocationEndpoint(EventLocationService eventLocationService, EventLocationMapper eventLocationMapper) {
        this.eventLocationService = eventLocationService;
        this.eventLocationMapper = eventLocationMapper;
    }

    @GetMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get event location by id", security = @SecurityRequirement(name = "apiKey"))
    public EventLocationDetailDto getEventLocationById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/locations/{}", id);
        return eventLocationMapper.eventLocationToEventLocationDetailDto(eventLocationService.getEventLocationById(id));
    }

    @GetMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all event locations", security = @SecurityRequirement(name = "apiKey"))
    public List<EventLocationDetailDto> getAllEventLocations() {
        LOGGER.info("GET /api/v1/locations");
        return eventLocationMapper.eventLocationsToEventLocationDtos(eventLocationService.getAllEventLocations());
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new event location", security = @SecurityRequirement(name = "apiKey"))
    public EventLocationDetailDto createEventLocation(@RequestBody @Valid CreateEventLocationDto createEventLocationDetailDto) {
        LOGGER.info("POST /api/v1/locations");
        EventLocation eventLocation = eventLocationService.createEventLocation(eventLocationMapper.createEventLocationDtoToEventLocation(createEventLocationDetailDto));
        return eventLocationMapper.eventLocationToEventLocationDetailDto(eventLocation);
    }
}
