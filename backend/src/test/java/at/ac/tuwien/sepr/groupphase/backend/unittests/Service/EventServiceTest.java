package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventTopTenDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.UpdateEventDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EventServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.EventValidator;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private TicketRepository ticketRepository;

    private EventServiceImpl eventService;

    private EventLocation testLocation;

    private ShowRepository showRepository;

    private EventMapper eventMapper;

    private ShowMapper showMapper;

    private Event event;

    private Long eventId;

    private EventDetailDto eventDetailDto;

    private Show mockShow;

    private ShowDetailDto mockShowDto;

    @BeforeEach
    public void setUp() {
        ticketRepository = mock(TicketRepository.class);
        showRepository = mock(ShowRepository.class);
        eventMapper = mock(EventMapper.class);
        showMapper = mock(ShowMapper.class);
        EventValidator eventValidator = new EventValidator(eventRepository, eventLocationRepository, showRepository);
        eventService =
            new EventServiceImpl(eventRepository, eventLocationRepository, showRepository, ticketRepository, eventMapper, showMapper, eventValidator);

        testLocation = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Test Location")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Test Street")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.OPERA)
            .build();

        eventLocationRepository.save(testLocation);

        event = Event.EventBuilder.anEvent()
            .withName("Test Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDuration(800)
            .withDateTime(LocalDateTime.now().plusDays(1))
            .withDescription("A beautiful classical concert.")
            .withLocation(testLocation)
            .build();

        eventRepository.save(event);

        eventId = event.getId();

        eventDetailDto = new EventDetailDto();
        eventDetailDto.setId(eventId);
        eventDetailDto.setName(event.getName());
        eventDetailDto.setCategory(event.getCategory().name());
        eventDetailDto.setDuration(event.getDuration());
        eventDetailDto.setDescription(event.getDescription());
        eventDetailDto.setDateTime(event.getDateTime());
        eventDetailDto.setLocationId(testLocation.getId());

        mockShow = new Show();
        mockShow.setId(10L);
        mockShow.setName("Test Show");
        mockShow.setDuration(90);
        mockShow.setEvent(event);

        mockShowDto = ShowDetailDto.ShowDetailDtoBuilder.aShowDetailDto()
            .id(10L)
            .name("Test Show")
            .duration(90)
            .eventId(eventId)
            .artistIds(Set.of(1L))
            .build();
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
            () -> assertEquals("Test Event", events.getFirst().getName()),
            () -> assertEquals(testLocation.getId(), events.getFirst().getLocation().getId())
        );
    }

    @Test
    public void testGetAllEvents_Paginated_returnsPagedUpdateEventDtos() {
        Event another = Event.EventBuilder.anEvent()
            .withName("Second Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDateTime(LocalDateTime.now().plusDays(2))
            .withDuration(60)
            .withDescription("Second")
            .withLocation(testLocation)
            .build();
        eventRepository.save(another);

        UpdateEventDto dto1 = new UpdateEventDto();
        dto1.setName(event.getName());
        dto1.setCategory(event.getCategory().name());
        dto1.setDescription(event.getDescription());
        dto1.setDateTime(event.getDateTime());
        dto1.setDuration(event.getDuration());
        dto1.setLocationId(testLocation.getId());

        UpdateEventDto dto2 = new UpdateEventDto();
        dto2.setName(another.getName());
        dto2.setCategory(another.getCategory().name());
        dto2.setDescription(another.getDescription());
        dto2.setDateTime(another.getDateTime());
        dto2.setDuration(another.getDuration());
        dto2.setLocationId(testLocation.getId());

        when(eventMapper.eventToUpdateEventDto(event)).thenReturn(dto1);
        when(eventMapper.eventToUpdateEventDto(another)).thenReturn(dto2);

        Pageable pageable = PageRequest.of(0, 2);
        Page<UpdateEventDto> page = eventService.getAllEventsPaginated(pageable);

        assertAll(
            () -> assertEquals(2, page.getTotalElements()),
            () -> assertTrue(
                page.getContent().stream().anyMatch(d -> d.getName().equals("Test Event")),
                "First Dto should have the name of the first event"
            ),
            () -> assertTrue(
                page.getContent().stream().anyMatch(d -> d.getName().equals("Second Event")),
                "Second Dto should have the name of the second event"
            )
        );

        verify(eventMapper).eventToUpdateEventDto(event);
        verify(eventMapper).eventToUpdateEventDto(another);
    }

    @Test
    public void testGetAllEvents_Paginated_empty_returnsEmptyPage() {
        eventRepository.deleteAll();

        Pageable pageable = PageRequest.of(0, 1);
        Page<UpdateEventDto> page = eventService.getAllEventsPaginated(pageable);

        assertAll(
            () -> assertEquals(0, page.getTotalElements()),
            () -> assertTrue(page.getContent().isEmpty())
        );
        verifyNoInteractions(eventMapper);
    }

    @Test
    @Transactional
    public void testCreateEvent_validEvent_savesSuccessfully() throws ValidationException {
        Event newEvent = Event.EventBuilder.anEvent()
            .withName("New Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDateTime(LocalDateTime.now().plusDays(2))
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
    @Transactional
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
    @Transactional
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

        when(showRepository.findEventsByArtistId(eq(artistId), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(event)));

        when(eventMapper.eventToEventDetailDto(event)).thenReturn(eventDetailDto);

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
    public void testGetPaginatedShowsForEvent_validEventId_returnsPaginatedShowDtos() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Show> showPage = new PageImpl<>(List.of(mockShow), pageable, 1);

        when(showRepository.findByEvent(any(Event.class), eq(pageable)))
            .thenReturn(showPage);
        when(showMapper.showToShowDetailDto(mockShow)).thenReturn(mockShowDto);

        Page<ShowDetailDto> result = eventService.getPaginatedShowsForEvent(eventId, pageable);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(1, result.getTotalElements()),
            () -> assertEquals(1, result.getContent().size()),
            () -> assertEquals(mockShowDto, result.getContent().getFirst())
        );

        verify(showRepository).findByEvent(any(Event.class), eq(pageable));
        verify(showMapper).showToShowDetailDto(mockShow);
    }

    @Test
    @Transactional
    public void testUpdateEvent_validChange_succeeds() throws ValidationException {
        when(showRepository.findByEventOrderByDateAsc(event))
            .thenReturn(List.of());

        Event updated = Event.EventBuilder.anEvent()
            .withName("New Name")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("New Desc")
            .withDateTime(event.getDateTime().plusHours(1))
            .withDuration(100)
            .withLocation(testLocation)
            .build();

        Event result = eventService.updateEvent(eventId, updated);

        assertAll(
            () -> assertEquals("New Name", result.getName()),
            () -> assertEquals("New Desc", result.getDescription()),
            () -> assertEquals(100, result.getDuration()),
            () -> assertEquals(testLocation.getId(), result.getLocation().getId())
        );
    }

    @Test
    @Transactional
    public void testUpdateEvent_blankName_throwsValidationException() {
        Event invalid = Event.EventBuilder.anEvent()
            .withName("   ")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("Desc")
            .withDateTime(event.getDateTime())
            .withDuration(120)
            .withLocation(testLocation)
            .build();

        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> eventService.updateEvent(eventId, invalid)
        );
        assertTrue(ex.getMessage().contains("Name must not be blank"));
    }

    @Test
    @Transactional
    public void testUpdateEvent_showOutsideNewTimeframe_throwsValidationException() {
        Show s1 = new Show();
        s1.setId(42L);
        s1.setDate(event.getDateTime().minusHours(2));
        s1.setDuration(30);
        s1.setEvent(event);

        when(showRepository.findByEventOrderByDateAsc(any(Event.class)))
            .thenReturn(List.of(s1));

        Event invalid = Event.EventBuilder.anEvent()
            .withName("Name")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("Desc")
            .withDateTime(event.getDateTime().plusHours(3))
            .withDuration(60)
            .withLocation(testLocation)
            .build();

        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> eventService.updateEvent(eventId, invalid)
        );
        assertTrue(ex.getMessage().contains("outside event timeframe"));
    }

    @Test
    @Transactional
    public void testUpdateEvent_invalidLocation_throwsValidationException() {
        Event invalid = Event.EventBuilder.anEvent()
            .withName("Name")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("Desc")
            .withDateTime(event.getDateTime())
            .withDuration(120)
            .withLocation(EventLocation.EventLocationBuilder.anEventLocation().build())
            .build();

        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> eventService.updateEvent(eventId, invalid)
        );
        assertTrue(ex.getMessage().contains("Location not found"));
    }

    @Test
    public void testGetTopTenEventsByCategory_validCategory_returnsList() throws ValidationException {
        Pageable topTen = PageRequest.of(0, 10);
        Event.EventCategory category = Event.EventCategory.CLASSICAL;

        Event mockEvent = new Event();
        mockEvent.setId(1L);
        mockEvent.setName("Event One");
        mockEvent.setDateTime(LocalDateTime.now());

        Object[] row = new Object[] {mockEvent, 5L};
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(row);

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(ticketRepository.findTopTenEventsByCategoryOrderByTicketCountDesc(eq(category), captor.capture(), eq(topTen)))
            .thenReturn(mockResults);

        List<EventTopTenDto> result = eventService.getTopTenEventsByCategory(category.name());

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Event One", result.getFirst().getName()),
            () -> assertEquals(5L, result.getFirst().getTicketsSold())
        );
    }

    @Test
    public void testGetTopTenEventsByCategory_allCategory_returnsList() throws ValidationException {
        Pageable topTen = PageRequest.of(0, 10);

        Event mockEvent = new Event();
        mockEvent.setId(2L);
        mockEvent.setName("Event Two");
        mockEvent.setDateTime(LocalDateTime.now());

        Object[] row = new Object[] {mockEvent, 8L};
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(row);

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(ticketRepository.findTopTenEventsOrderByTicketCountDesc(captor.capture(), eq(topTen)))
            .thenReturn(mockResults);

        List<EventTopTenDto> result = eventService.getTopTenEventsByCategory("all");

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Event Two", result.getFirst().getName()),
            () -> assertEquals(8L, result.getFirst().getTicketsSold())
        );
    }

    @Test
    public void testGetTopTenEventsByCategory_invalidCategory_throwsValidationException() {
        String invalidCategory = "INVALID_CAT";

        assertThrows(ValidationException.class, () -> {
            eventService.getTopTenEventsByCategory(invalidCategory);
        });
    }

    @Test
    public void testGetTopTenEventsByCategory_emptyResult_returnsEmptyList() throws ValidationException {
        Pageable topTen = PageRequest.of(0, 10);

        when(ticketRepository.findTopTenEventsOrderByTicketCountDesc(LocalDateTime.now().plusDays(30), topTen)).thenReturn(List.of());

        List<EventTopTenDto> result = eventService.getTopTenEventsByCategory("all");

        assertAll(
            () -> assertNotNull(result),
            () -> assertTrue(result.isEmpty())
        );
    }

}