package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.service.validators.EventValidator;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EventServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

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

    @BeforeEach
    public void setUp() {
        eventService = new EventServiceImpl(eventRepository, eventLocationRepository, eventValidator);

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
}
