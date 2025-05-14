package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreateEventDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.EventService;
import at.ac.tuwien.sepr.groupphase.backend.service.SearchService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    private final SearchService searchService;
    private final ShowService showService;
    private final ShowMapper showMapper;

    @Autowired
    public EventEndpoint(EventService eventService, EventMapper eventMapper, SearchService searchService, ShowService showService, ShowMapper showMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
        this.searchService = searchService;
        this.showService = showService;
        this.showMapper = showMapper;
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

    /**
     * Searches for events based on the provided search criteria.
     *
     * @param eventSearchDto the search criteria
     * @return a paginated list of events matching the search criteria
     */
    @Secured("ROLE_USER")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Search events",
        description = "Search events by name, type, description, or duration (±30min) with page and size parameters.",
        security = @SecurityRequirement(name = "apiKey")
    )
    public Page<EventSearchResultDto> search(@Valid EventSearchDto eventSearchDto) throws ValidationException {
        LOGGER.info("GET /api/v1/events/search {}", eventSearchDto);
        return searchService.searchEvents(eventSearchDto);
    }

    /**
     * Retrieves all shows for a specific event.
     *
     * @param eventId the ID of the event
     * @return a list of shows for the specified event
     */
    @Secured("ROLE_USER")
    @GetMapping("/{eventId}/shows")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get shows for a specific event",
        description = "Returns all shows of the given event, sorted by date.",
        security = @SecurityRequirement(name = "apiKey")
    )
    public List<ShowDetailDto> getShowsForEvent(@PathVariable("eventId") Long eventId) {
        LOGGER.info("GET /api/v1/events/{}/shows", eventId);
        return showMapper.showsToShowDetailDtos(showService.findShowsByEventId(eventId));
    }

    @Secured("ROLE_USER")
    @GetMapping("/by-artist/{artistId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get all events for a specific artist",
        description = "Returns paginated events linked to the given artist ID via shows.",
        security = @SecurityRequirement(name = "apiKey")
    )
    public Page<EventDetailDto> getEventsByArtist(
        @PathVariable("artistId") Long artistId,
        org.springframework.data.domain.Pageable pageable
    ) {
        LOGGER.info("GET /api/v1/events/by-artist/{}?page={}&size={}", artistId, pageable.getPageNumber(), pageable.getPageSize());
        return eventService.getEventsByArtist(artistId, pageable);
    }
}
