package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.CreateShowDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.SearchService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
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
import org.springframework.data.domain.Page;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("api/v1/shows")
public class ShowEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ShowService showService;
    private final ShowMapper showMapper;
    private final SearchService searchService;

    @Autowired
    public ShowEndpoint(ShowService showService, ShowMapper showMapper, SearchService searchService) {
        this.showService = showService;
        this.showMapper = showMapper;
        this.searchService = searchService;
    }

    @GetMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get show by id", security = @SecurityRequirement(name = "apiKey"))
    public ShowDetailDto getShowById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/shows/{}", id);
        return showMapper.showToShowDetailDto(showService.getShowById(id));
    }

    @GetMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all shows", security = @SecurityRequirement(name = "apiKey"))
    public List<ShowDetailDto> getAllShows() {
        LOGGER.info("GET /api/v1/shows");
        return showMapper.showsToShowDetailDtos(showService.getAllShows());
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new show", security = @SecurityRequirement(name = "apiKey"))
    public ShowDetailDto createShow(@RequestBody @Valid CreateShowDto createShowDto) throws ValidationException {
        LOGGER.info("POST /api/v1/shows");
        Show show = showService.createShow(showMapper.createShowDtoToShow(createShowDto));
        return showMapper.showToShowDetailDto(show);
    }

    @PostMapping("/search")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search for shows by various filter criteria", security = @SecurityRequirement(name = "apiKey"))
    public Page<ShowSearchResultDto> searchShows(@RequestBody @Valid ShowSearchDto searchDto) throws ValidationException {
        LOGGER.info("POST /api/v1/shows/search with criteria: {}", searchDto);
        return searchService.searchShows(searchDto);
    }
}
