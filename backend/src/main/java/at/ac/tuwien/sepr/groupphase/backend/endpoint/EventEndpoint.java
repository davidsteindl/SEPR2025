package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreateArtistDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreateEventDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreateEventLocationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventLocationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ArtistMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventLocationMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
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
    private final EventLocationMapper eventLocationMapper;
    private final ArtistMapper artistMapper;

    @Autowired
    public EventEndpoint(EventService eventService, EventMapper eventMapper, EventLocationMapper eventLocationMapper, ArtistMapper artistMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
        this.eventLocationMapper = eventLocationMapper;
        this.artistMapper = artistMapper;
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
    public EventDetailDto createEvent(@RequestBody @Valid CreateEventDto createEventDto) {
        LOGGER.info("POST /api/v1/events");
        Event event = eventService.createEvent(eventMapper.createEventDtoToEvent(createEventDto));
        return eventMapper.eventToEventDetailDto(event);
    }

    @GetMapping("/locations/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get event location by id", security = @SecurityRequirement(name = "apiKey"))
    public EventLocationDetailDto getEventLocationById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/events/locations/{}", id);
        return eventLocationMapper.eventLocationToEventLocationDetailDto(eventService.getEventLocationById(id));
    }

    @GetMapping("/locations")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all event locations", security = @SecurityRequirement(name = "apiKey"))
    public List<EventLocationDetailDto> getAllEventLocations() {
        LOGGER.info("GET /api/v1/events/locations");
        return eventLocationMapper.eventLocationsToEventLocationDtos(eventService.getAllEventLocations());
    }

    @PostMapping("/locations")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new event location", security = @SecurityRequirement(name = "apiKey"))
    public EventLocationDetailDto createEventLocation(@RequestBody @Valid CreateEventLocationDto createEventLocationDetailDto) {
        LOGGER.info("POST /api/v1/events/locations");
        EventLocation eventLocation = eventService.createEventLocation(eventLocationMapper.createEventLocationDtoToEventLocation(createEventLocationDetailDto));
        return eventLocationMapper.eventLocationToEventLocationDetailDto(eventLocation);
    }

    @GetMapping("/artists/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get artist by id", security = @SecurityRequirement(name = "apiKey"))
    public ArtistDetailDto getArtistById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/events/artists/{}", id);
        return artistMapper.artistToArtistDetailDto(eventService.getArtistById(id));
    }

    @GetMapping("/artists")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all artists", security = @SecurityRequirement(name = "apiKey"))
    public List<ArtistDetailDto> getAllArtists() {
        LOGGER.info("GET /api/v1/events/artists");
        return artistMapper.artistsToArtistDetailDtos(eventService.getAllArtists());
    }

    @PostMapping("/artists")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new artist", security = @SecurityRequirement(name = "apiKey"))
    public ArtistDetailDto createArtist(@RequestBody @Valid CreateArtistDto createArtistDto) {
        LOGGER.info("POST /api/v1/events/artists");
        Artist artist = eventService.createArtist(artistMapper.createArtistDtoToArtist(createArtistDto));
        return artistMapper.artistToArtistDetailDto(artist);
    }
}
