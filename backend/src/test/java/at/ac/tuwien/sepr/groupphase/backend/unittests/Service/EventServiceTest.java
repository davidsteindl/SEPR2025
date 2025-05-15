package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.EventValidator;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EventServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class EventServiceTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    private EventServiceImpl eventService;

    private EventLocation testLocation;

    @Autowired
    private EventValidator eventValidator;

    private ShowRepository showRepository;

    private EventMapper eventMapper;

    private ShowMapper showMapper;

    @BeforeEach
    public void setUp() {
        showRepository = mock(ShowRepository.class);
        eventMapper = mock(EventMapper.class);
        showMapper = mock(ShowMapper.class);
        eventService = new EventServiceImpl(eventRepository, eventLocationRepository, eventValidator, showRepository, eventMapper, showMapper);

        testLocation = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Test Location")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Test Street")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.OPERA)
            .build();

        eventLocationRepository.save(testLocation);

        Event event = Event.EventBuilder.anEvent()
            .withName("Test Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDuration(800)
            .withDescription("A beautiful classical concert.")
            .withLocation(testLocation)
            .build();

        eventRepository.save(event);
    }

    @AfterEach
    public void deleteData() {
        eventRepository.deleteAll();
        eventLocationRepository.deleteAll();
    }

    @Test
    public void testGetEventById_existingId_returnsEvent() {
        Event event = eventRepository.findAll().getFirst();
        Event result = eventService.getEventById(event.getId());

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals("Test Event", result.getName()),
            () -> assertEquals(testLocation.getId(), result.getLocation().getId())
        );
    }

    @Test
    public void testGetEventById_nonExistingId_returnsNull() {
        Event result = eventService.getEventById(999L);

        assertAll(
            () -> assertNull(result),
            () -> assertEquals(1, eventRepository.findAll().size())
        );
    }

    @Test
    public void testGetAllEvents_returnsList() {
        List<Event> events = eventService.getAllEvents();

        assertAll(
            () -> assertEquals(1, events.size()),
            () -> assertEquals("Test Event", events.get(0).getName()),
            () -> assertEquals(testLocation.getId(), events.get(0).getLocation().getId())
        );
    }

    @Test
    public void testCreateEvent_validEvent_savesSuccessfully() throws ValidationException {
        Event newEvent = Event.EventBuilder.anEvent()
            .withName("New Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDuration(120)
            .withDescription("A new classical concert.")
            .withLocation(testLocation)
            .build();

        Event savedEvent = eventService.createEvent(newEvent);

        assertAll(
            () -> assertNotNull(savedEvent.getId()),
            () -> assertEquals("New Event", savedEvent.getName()),
            () -> assertEquals(testLocation.getId(), savedEvent.getLocation().getId()),
            () -> assertEquals(2, eventRepository.findAll().size())
        );
    }

    @Test
    public void testCreateEvent_nullLocation_throwsValidationException() {
        Event newEvent = Event.EventBuilder.anEvent()
            .withName("Invalid Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withLocation(null)
            .build();

        assertAll(
            () -> assertThrows(ValidationException.class, () -> eventService.createEvent(newEvent)),
            () -> assertEquals(1, eventRepository.findAll().size())
        );
    }

    @Test
    public void testCreateEvent_nonExistingLocation_throwsValidationException() {
        EventLocation fakeLocation = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Fake Location")
            .build();
        fakeLocation.setId(999L); // non-existing location

        Event newEvent = Event.EventBuilder.anEvent()
            .withName("Invalid Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withLocation(fakeLocation)
            .build();

        assertAll(
            () -> assertThrows(ValidationException.class, () -> eventService.createEvent(newEvent)),
            () -> assertEquals(1, eventRepository.findAll().size())
        );
    }

    @Test
    public void testGetEventsByArtist_returnsMappedDto() {
        Long artistId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        Event event = eventRepository.findAll().getFirst();

        EventDetailDto dto = new EventDetailDto();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setCategory(event.getCategory().name());
        dto.setLocationId(testLocation.getId());

        when(showRepository.findEventsByArtistId(eq(artistId), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(event)));

        when(eventMapper.eventToEventDetailDto(event)).thenReturn(dto);

        Page<EventDetailDto> result = eventService.getEventsByArtist(artistId, pageable);

        assertAll(
            () -> assertEquals(1, result.getTotalElements()),
            () -> assertEquals("Test Event", result.getContent().getFirst().getName()),
            () -> assertEquals("CLASSICAL", result.getContent().getFirst().getCategory())
        );

        verify(showRepository).findEventsByArtistId(artistId, pageable);
        verify(eventMapper).eventToEventDetailDto(event);
    }

    @Test
    public void testGetEventWithShows_validEventId_returnsEventWithShowsDto() {
        Event event = eventRepository.findAll().getFirst();
        Long eventId = event.getId();

        EventDetailDto dto = new EventDetailDto();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setCategory(event.getCategory().name());
        dto.setLocationId(testLocation.getId());

        Show mockShow = new Show();
        mockShow.setId(10L);
        mockShow.setName("Test Show");
        mockShow.setDuration(90);
        mockShow.setEvent(event);

        ShowDetailDto mockShowDto = ShowDetailDto.ShowDetailDtoBuilder.aShowDetailDto()
            .id(10L)
            .name("Test Show")
            .duration(90)
            .eventId(eventId)
            .artistIds(Set.of(1L))
            .build();

        when(eventMapper.eventToEventDetailDto(event)).thenReturn(dto);
        when(showRepository.findByEventOrderByDateAscWithArtists(event)).thenReturn(List.of(mockShow));
        when(showMapper.showsToShowDetailDtos(List.of(mockShow))).thenReturn(List.of(mockShowDto));

        var result = eventService.getEventWithShows(eventId);

        assertAll(
            () -> assertNotNull(result, "Result should not be null"),
            () -> assertEquals(dto, result.getEvent(), "Event DTO should match"),
            () -> assertEquals(1, result.getShows().size(), "There should be exactly one show"),
            () -> assertEquals(mockShowDto, result.getShows().getFirst(), "Show DTO should match")
        );

        verify(eventMapper).eventToEventDetailDto(event);
        verify(showRepository).findByEventOrderByDateAscWithArtists(event);
        verify(showMapper).showsToShowDetailDtos(List.of(mockShow));
    }


}
