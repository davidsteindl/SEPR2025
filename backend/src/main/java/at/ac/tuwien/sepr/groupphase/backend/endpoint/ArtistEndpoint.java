package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreateArtistDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ArtistMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.ArtistService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("api/v1/artists")
public class ArtistEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ArtistService artistService;
    private final ArtistMapper artistMapper;
    private final SearchService searchService;

    @Autowired
    public ArtistEndpoint(ArtistService artistService, ArtistMapper artistMapper, SearchService searchService) {
        this.artistService = artistService;
        this.artistMapper = artistMapper;
        this.searchService = searchService;
    }

    @GetMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get artist by id", security = @SecurityRequirement(name = "apiKey"))
    public ArtistDetailDto getArtistById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/artists/{}", id);
        return artistMapper.artistToArtistDetailDto(artistService.getArtistById(id));
    }

    @GetMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all artists", security = @SecurityRequirement(name = "apiKey"))
    public List<ArtistDetailDto> getAllArtists() {
        LOGGER.info("GET /api/v1/artists");
        return artistMapper.artistsToArtistDetailDtos(artistService.getAllArtists());
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new artist", security = @SecurityRequirement(name = "apiKey"))
    public ArtistDetailDto createArtist(@RequestBody @Valid CreateArtistDto createArtistDto) throws ValidationException {
        LOGGER.info("POST /api/v1/artists");

        Artist artist = artistService.createArtist(artistMapper.createArtistDtoToArtist(createArtistDto));
        return artistMapper.artistToArtistDetailDto(artist);
    }

    @PostMapping("/search")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Search artists by firstname, lastname or stagename",
        description = "Returns a paginated list of artists matching the given search criteria.",
        security = @SecurityRequirement(name = "apiKey")
    )
    public Page<ArtistSearchResultDto> searchArtists(@RequestBody @Valid ArtistSearchDto searchDto) throws ValidationException {
        LOGGER.info("POST /api/v1/artists/search with payload: {}", searchDto);
        return searchService.searchArtists(searchDto);
    }

}
