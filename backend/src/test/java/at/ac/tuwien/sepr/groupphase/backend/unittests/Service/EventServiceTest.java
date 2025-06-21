package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventTopTenDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.UpdateEventDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EventServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.EventValidator;
import jakarta.transaction.Transactional;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private ShowMapper showMapper;

    private EventServiceImpl eventService;

    private EventLocation testLocation;
    private Event event;
    private Long eventId;

    @BeforeEach
    public void setUp() {
        EventValidator eventValidator = new EventValidator(eventRepository, eventLocationRepository, showRepository);
        eventService = new EventServiceImpl(eventRepository, eventLocationRepository, showRepository, ticketRepository, eventMapper, showMapper, eventValidator);

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
    }

    @AfterEach
    public void deleteData() {
        for (Show show : showRepository.findAll()) {
            show.getArtists().clear();
        }
        showRepository.deleteAll();
        ticketRepository.deleteAll();
        eventRepository.deleteAll();
        roomRepository.deleteAll();
        eventLocationRepository.deleteAll();
    }

    @Test
    public void testGetEventById_existingId_returnsEvent() {
        Event result = eventService.getEventById(eventId);

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
            () -> assertTrue(eventRepository.existsById(eventId))
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

        Pageable pageable = PageRequest.of(0, 2);
        Page<UpdateEventDto> page = eventService.getAllEventsPaginated(pageable);

        assertAll(
            () -> assertEquals(2, page.getTotalElements()),
            () -> assertEquals(2, page.getContent().size()),
            () -> assertTrue(page.getContent().stream().anyMatch(d -> d.getName().equals("Test Event"))),
            () -> assertTrue(page.getContent().stream().anyMatch(d -> d.getName().equals("Second Event")))
        );
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
    }

    @Test
    public void testGetEventsByArtist_returnsMappedDto() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<EventDetailDto> result = eventService.getEventsByArtist(1L, pageable);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(0, result.getTotalElements()),
            () -> assertTrue(result.getContent().isEmpty())
        );
    }

    @Test
    public void testGetPaginatedShowsForEvent_validEventId_returnsPaginatedShowDtos() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<ShowDetailDto> result = eventService.getPaginatedShowsForEvent(eventId, pageable);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(0, result.getTotalElements()),
            () -> assertTrue(result.getContent().isEmpty())
        );
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
            () -> assertTrue(eventRepository.existsById(savedEvent.getId()))
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
            () -> assertTrue(eventRepository.existsById(eventId))
        );
    }

    @Test
    @Transactional
    public void testCreateEvent_nonExistingLocation_throwsValidationException() {
        EventLocation fakeLocation = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Fake Location")
            .build();
        fakeLocation.setId(999L);

        Event newEvent = Event.EventBuilder.anEvent()
            .withName("Invalid Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withLocation(fakeLocation)
            .build();

        assertAll(
            () -> assertThrows(ValidationException.class, () -> eventService.createEvent(newEvent)),
            () -> assertTrue(eventRepository.existsById(eventId))
        );
    }

    @Test
    @Transactional
    public void testUpdateEvent_validChange_succeeds() throws ValidationException {
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
        Room room = Room.RoomBuilder.aRoom()
            .withName("Test Room")
            .withEventLocation(testLocation)
            .build();
        roomRepository.save(room);

        Show s1 = Show.ShowBuilder.aShow()
            .withName("Conflict Show")
            .withDate(event.getDateTime().minusHours(2))
            .withDuration(30)
            .withEvent(event)
            .withRoom(room)
            .build();
        showRepository.save(s1);

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
        List<EventTopTenDto> result = eventService.getTopTenEventsByCategory(Event.EventCategory.CLASSICAL.name());

        assertAll(
            () -> assertNotNull(result),
            () -> assertTrue(result.size() >= 0)
        );
    }

    @Test
    public void testGetTopTenEventsByCategory_allCategory_returnsList() throws ValidationException {
        List<EventTopTenDto> result = eventService.getTopTenEventsByCategory("all");

        assertAll(
            () -> assertNotNull(result),
            () -> assertTrue(result.size() >= 0)
        );
    }

    @Test
    public void testGetTopTenEventsByCategory_invalidCategory_throwsValidationException() {
        String invalidCategory = "INVALID_CAT";

        assertThrows(ValidationException.class, () -> eventService.getTopTenEventsByCategory(invalidCategory));
    }

    @Test
    public void testGetTopTenEventsByCategory_emptyResult_returnsEmptyList() throws ValidationException {
        eventRepository.deleteAll();
        List<EventTopTenDto> result = eventService.getTopTenEventsByCategory("all");

        assertAll(
            () -> assertNotNull(result),
            () -> assertTrue(result.isEmpty())
        );
    }
}
