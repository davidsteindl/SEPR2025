package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.CreateEventLocationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventLocationMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.EventLocationService;
import at.ac.tuwien.sepr.groupphase.backend.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final SearchService searchService;
    private final ShowMapper showMapper;

    @Autowired
    public EventLocationEndpoint(EventLocationService eventLocationService, EventLocationMapper eventLocationMapper, SearchService searchService,
                                 ShowMapper showMapper) {
        this.eventLocationService = eventLocationService;
        this.eventLocationMapper = eventLocationMapper;
        this.searchService = searchService;
        this.showMapper = showMapper;
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
        EventLocation eventLocation =
            eventLocationService.createEventLocation(eventLocationMapper.createEventLocationDtoToEventLocation(createEventLocationDetailDto));
        return eventLocationMapper.eventLocationToEventLocationDetailDto(eventLocation);
    }


    /**
     * Searches for event locations based on the given search criteria.
     *
     * @param eventLocationSearchDto the search criteria
     * @return a page of event locations matching the search criteria
     */
    @PostMapping("/search")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Search eventlocations",
        description = "Search eventlocations by name, street, city, country or postal code with page and size parameters.",
        security = @SecurityRequirement(name = "apiKey")
    )
    public Page<EventLocationDetailDto> search(@RequestBody @Valid EventLocationSearchDto eventLocationSearchDto) throws ValidationException {
        LOGGER.info("POST /api/v1/locations/search {}", eventLocationSearchDto);
        return searchService.searchEventLocations(eventLocationSearchDto);
    }

    /**
     * Retrieves all shows for a specific event location.
     *
     * @param eventLocationId the ID of the event location
     * @return a list of shows for the specified event location
     */
    @Secured("ROLE_USER")
    @GetMapping("/{eventLocationId}/shows/paginated")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Get paginated shows for a specific event location",
        description = "Returns paginated list of all shows of the given event location, sorted by date.",
        security = @SecurityRequirement(name = "apiKey")
    )
    public Page<ShowDetailDto> getShowsForEventLocation(@PathVariable("eventLocationId") Long eventLocationId,
                                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                                        @RequestParam(name = "size", defaultValue = "5") int size) {
        LOGGER.info("GET /api/v1/locations/{}/shows/paginated?page={}&size={}", eventLocationId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());
        return eventLocationService.getShowsForEventLocation(eventLocationId, pageable);
    }
}
