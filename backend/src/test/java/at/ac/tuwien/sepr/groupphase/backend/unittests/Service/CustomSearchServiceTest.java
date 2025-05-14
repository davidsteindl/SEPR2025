package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ArtistMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event.EventCategory;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.CustomSearchService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.SearchValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomSearchServiceTest {
    @Mock
    private ArtistRepository artistRepo;
    @Mock
    private ArtistMapper artistMapper;

    @Mock
    private EventRepository eventRepo;
    @Mock
    private SearchValidator validator;
    @InjectMocks
    private CustomSearchService service;

    private Artist artist;
    private ArtistSearchResultDto dto;

    private Event event;

    @BeforeEach
    void setUp() throws Exception {
        artist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Freddie")
            .withLastname("Mercury")
            .withStagename("Queen")
            .build();
        dto = ArtistSearchResultDto.ArtistSearchResultDtoBuilder.anArtistSearchResultDto()
            .id(42L)
            .firstname("Freddie")
            .lastname("Mercury")
            .stagename("Queen")
            .build();

        event = new Event();
        event.setId(1L);
        event.setName("Test");
        event.setCategory(EventCategory.ROCK);
        EventLocation loc = new EventLocation(); loc.setId(2L);
        event.setLocation(loc);
        event.setDescription("Description");
        Field f = Event.class.getDeclaredField("totalDuration");
        f.setAccessible(true);
        f.set(event, 90);
    }

    @Test
    void givenValidSearchDto_whenSearchArtists_thenReturnsMappedDtoPage() throws ValidationException {
        ArtistSearchDto searchDto = new ArtistSearchDto();
        searchDto.setFirstname("Fred");
        searchDto.setPage(0);
        searchDto.setSize(10);

        Page<Artist> stubPage = new PageImpl<>(List.of(artist), PageRequest.of(0, 10), 1);
        when(artistRepo.findAll(
            ArgumentMatchers.<Specification<Artist>>any(),
            any(Pageable.class))
        ).thenReturn(stubPage);

        Page<ArtistSearchResultDto> result = service.searchArtists(searchDto);

        assertAll(
            () -> assertEquals(1, result.getTotalElements()),
            () -> assertEquals("Freddie", result.getContent().getFirst().getFirstname()),
            () -> assertEquals("Mercury", result.getContent().getFirst().getLastname()),
            () -> assertEquals("Queen", result.getContent().getFirst().getStagename())
        );

        verify(validator).validateForArtists(searchDto);
        verify(artistRepo).findAll(
            ArgumentMatchers.<Specification<Artist>>any(),
            eq(PageRequest.of(0, 10))
        );
    }

    @Test
    void givenNoMatches_whenSearchArtists_thenReturnsEmptyPage() throws ValidationException {
        ArtistSearchDto searchDto = new ArtistSearchDto();
        searchDto.setFirstname("Unknown");
        searchDto.setPage(0);
        searchDto.setSize(10);

        when(artistRepo.findAll(
            ArgumentMatchers.<Specification<Artist>>any(),
            any(Pageable.class))
        ).thenReturn(Page.empty());

        Page<ArtistSearchResultDto> result = service.searchArtists(searchDto);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(0, result.getTotalElements()),
            () -> assertTrue(result.getContent().isEmpty())
        );

        verify(validator).validateForArtists(searchDto);
        verify(artistRepo).findAll(
            ArgumentMatchers.<Specification<Artist>>any(),
            eq(PageRequest.of(0, 10))
        );
    }

    @Test
    void searchEvents_MapsEntitiesToDto() throws ValidationException {
        Page<Event> stubPage = new PageImpl<>(List.of(event), PageRequest.of(0,10), 1);

        when(eventRepo.findAll(
            ArgumentMatchers.<Specification<Event>>any(),
            any(Pageable.class)
        )).thenReturn(stubPage);

        doNothing().when(validator).validateForEvents(any(EventSearchDto.class));

        EventSearchDto dto = new EventSearchDto();
        dto.setPage(0);
        dto.setSize(10);

        Page<EventSearchResultDto> result = service.searchEvents(dto);

        assertEquals(1, result.getTotalElements());
        EventSearchResultDto out = result.getContent().getFirst();
        assertEquals(1L, out.getId());
        assertEquals("Test", out.getName());
        assertEquals("Rock", out.getCategory());
        assertEquals(2L, out.getLocationId());
        assertEquals(90, out.getDuration());
        assertEquals("Description", out.getDescription());

        verify(validator).validateForEvents(dto);
    }
}
