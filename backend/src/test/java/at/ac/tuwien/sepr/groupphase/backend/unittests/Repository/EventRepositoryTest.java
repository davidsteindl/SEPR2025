package at.ac.tuwien.sepr.groupphase.backend.unittests.Repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        EventLocation eventLocation = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Test Location")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Test Street")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.OPERA)
            .build();

        eventLocationRepository.save(eventLocation);

        LocalDateTime futureDate = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES);
        Event event = Event.EventBuilder.anEvent()
            .withName("Test Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("A wonderful classical evening.")
            .withDateTime(futureDate)
            .withDuration(120)
            .withLocation(eventLocation)
            .build();

        eventRepository.save(event);
    }

    @AfterEach
    public void deleteData() {
        eventRepository.deleteAll();
        eventLocationRepository.deleteAll();
    }

    @Test
    public void findAllAndGetSize1() {
        assertEquals(1, eventRepository.findAll().size());
    }

    @Test
    public void findById() {
        Event event = eventRepository.findAll().getFirst();
        assertAll(
            () -> assertNotNull(eventRepository.findById(event.getId())),
            () -> assertNotNull(event.getId()),
            () -> assertEquals("Test Event", event.getName()),
            () -> assertEquals(Event.EventCategory.CLASSICAL, event.getCategory()),
            () -> assertEquals("Test Location", event.getLocation().getName()),
            () -> assertEquals("A wonderful classical evening.", event.getDescription()),
            () -> assertEquals(120, event.getDuration())
        );
    }

    @Test
    public void testSaveEvent_withNullName_throwsException() {
        EventLocation location = eventLocationRepository.findAll().getFirst();

        Event event = Event.EventBuilder.anEvent()
            .withName(null)
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("Some Description")
            .withDuration(100)
            .withLocation(location)
            .build();

        assertThrows(Exception.class, () -> eventRepository.saveAndFlush(event));
        entityManager.clear();
    }

    @Test
    public void testSaveEvent_withNullCategory_throwsException() {
        EventLocation location = eventLocationRepository.findAll().getFirst();

        Event event = Event.EventBuilder.anEvent()
            .withName("Unnamed Event")
            .withCategory(null)
            .withDescription("Some Description")
            .withDuration(100)
            .withLocation(location)
            .build();

        assertThrows(Exception.class, () -> eventRepository.saveAndFlush(event));
        entityManager.clear();
    }

    @Test
    public void testSaveEvent_withNullLocation_throwsException() {
        Event event = Event.EventBuilder.anEvent()
            .withName("New Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("Some Description")
            .withDuration(100)
            .withLocation(null)
            .build();

        assertThrows(Exception.class, () -> eventRepository.saveAndFlush(event));
        entityManager.clear();
    }

    @Test
    public void testSaveEvent_withAllRequiredFields_savesSuccessfully() {
        EventLocation location = eventLocationRepository.findAll().getFirst();

        Event event = Event.EventBuilder.anEvent()
            .withName("Jazz Fest")
            .withCategory(Event.EventCategory.JAZZ)
            .withDescription("Smooth jazz all night long.")
            .withDateTime(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES))
            .withDuration(120)
            .withLocation(location)
            .build();

        Event saved = eventRepository.save(event);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals("Jazz Fest", saved.getName()),
            () -> assertEquals("Smooth jazz all night long.", saved.getDescription()),
            () -> assertEquals(120, saved.getDuration())
        );
    }
}
