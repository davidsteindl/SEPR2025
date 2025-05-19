package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ArtistMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event.EventCategory;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
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
import java.time.LocalDateTime;
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
    @Mock
    private ShowRepository showRepo;

    private Artist artist;
    private ArtistSearchResultDto dto;
    private Event event;
    private Show show;
    private Room room;

    private EventLocation location;

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
        Field f = Event.class.getDeclaredField("duration");
        f.setAccessible(true);
        f.set(event, 90);

        location = new EventLocation();
        location.setId(5L);
        location.setName("Gasometer");
        location.setStreet("Guglgasse 6");
        location.setCity("Vienna");
        location.setCountry("Austria");
        location.setPostalCode("1110");

        room = new Room();
        room.setId(3L);
        room.setName("Main Hall");

        show = Show.ShowBuilder.aShow()
            .withName("Amazing Show")
            .withDuration(120)
            .withDate(LocalDateTime.of(2025, 6, 1, 20, 0))
            .withEvent(event)
            .withRoom(room)
            .build();

        Field idField = Show.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(show, 100L);
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
    void givenValidSearchDto_whenSearchEventLocations_thenReturnsMappedDtoPage() throws ValidationException {
        Page<EventLocation> stubPage = new PageImpl<>(List.of(location), PageRequest.of(0, 10), 1);
        when(eventLocationRepo.findAll(
            ArgumentMatchers.<Specification<EventLocation>>any(),
            any(Pageable.class)))
            .thenReturn(stubPage);

        doNothing().when(validator).validateForEventLocations(any(EventLocationSearchDto.class));

        EventLocationSearchDto searchDto = new EventLocationSearchDto();
        searchDto.setPage(0);
        searchDto.setSize(10);

        Page<EventLocationDetailDto> result = service.searchEventLocations(searchDto);

        assertEquals(1, result.getTotalElements());
        EventLocationDetailDto out = result.getContent().get(0);
        assertAll(
            () -> assertEquals(5L, out.getId()),
            () -> assertEquals("Gasometer", out.getName()),
            () -> assertEquals("Guglgasse 6", out.getStreet()),
            () -> assertEquals("Vienna", out.getCity()),
            () -> assertEquals("Austria", out.getCountry()),
            () -> assertEquals("1110", out.getPostalCode())
        );

        verify(validator).validateForEventLocations(searchDto);
        verify(eventLocationRepo).findAll(
            ArgumentMatchers.<Specification<EventLocation>>any(),
            eq(PageRequest.of(0, 10))
        );
    }

    @Test
    void givenNoMatches_whenSearchEventLocations_thenReturnsEmptyPage() throws ValidationException {
        when(eventLocationRepo.findAll(
            ArgumentMatchers.<Specification<EventLocation>>any(),
            any(Pageable.class)))
            .thenReturn(Page.empty());

        doNothing().when(validator).validateForEventLocations(any(EventLocationSearchDto.class));

        EventLocationSearchDto searchDto = new EventLocationSearchDto();
        searchDto.setPage(0);
        searchDto.setSize(10);

        Page<EventLocationDetailDto> result = service.searchEventLocations(searchDto);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(validator).validateForEventLocations(searchDto);
        verify(eventLocationRepo).findAll(
            ArgumentMatchers.<Specification<EventLocation>>any(),
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

    @Test
    void givenValidSearchDto_whenSearchShows_thenReturnsMappedResultPage() throws ValidationException {
        ShowSearchDto searchDto = new ShowSearchDto();
        searchDto.setPage(0);
        searchDto.setSize(10);
        searchDto.setEventName("Test");
        searchDto.setRoomName("Main");

        Page<Show> stubPage = new PageImpl<>(List.of(show), PageRequest.of(0, 10), 1);
        when(showRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(stubPage);
        doNothing().when(validator).validateForShows(searchDto);

        Page<ShowSearchResultDto> result = service.searchShows(searchDto);

        assertEquals(1, result.getTotalElements());
        ShowSearchResultDto dto = result.getContent().getFirst();
        assertEquals(100L, dto.getId());
        assertEquals("Amazing Show", dto.getName());
        assertEquals(120, dto.getDuration());
        assertEquals(LocalDateTime.of(2025, 6, 1, 20, 0), dto.getDate());
        assertEquals(1L, dto.getEventId());
        assertEquals("Test", dto.getEventName());
        assertEquals(3L, dto.getRoomId());
        assertEquals("Main Hall", dto.getRoomName());

        verify(validator).validateForShows(searchDto);
        verify(showRepo).findAll(any(Specification.class), eq(PageRequest.of(0, 10)));
    }

    @Test
    void givenNoMatchingShows_whenSearchShows_thenReturnsEmptyPage() throws ValidationException {
        ShowSearchDto searchDto = new ShowSearchDto();
        searchDto.setPage(0);
        searchDto.setSize(10);
        searchDto.setName("NotExisting");

        when(showRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        doNothing().when(validator).validateForShows(searchDto);

        Page<ShowSearchResultDto> result = service.searchShows(searchDto);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(validator).validateForShows(searchDto);
        verify(showRepo).findAll(any(Specification.class), eq(PageRequest.of(0, 10)));
    }
}
