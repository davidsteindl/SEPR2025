package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import at.ac.tuwien.sepr.groupphase.backend.service.SearchService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class SearchEventEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final SearchService searchService;
    private final ShowService showService;
    private final ShowMapper showMapper;

    @Autowired
    public SearchEventEndpoint(SearchService searchService, ShowService showService, ShowMapper showMapper) {
        this.searchService = searchService;
        this.showService = showService;
        this.showMapper = showMapper;
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
        summary = "Search and filter events with pagination",
        description = "Search events by name, type, description, or duration (±30min) with page and size parameters.",
        security = @SecurityRequirement(name = "apiKey")
    )
    public Page<EventSearchResultDto> search(@Valid EventSearchDto eventSearchDto) {
        LOGGER.info("GET /api/v1/events");
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
        return showMapper.showsToShowDetailDtos(
            showService.findShowsByEventId(eventId)
        );
    }
}