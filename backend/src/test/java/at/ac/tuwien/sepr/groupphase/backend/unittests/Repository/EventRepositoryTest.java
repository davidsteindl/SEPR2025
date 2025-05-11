package at.ac.tuwien.sepr.groupphase.backend.unittests.Repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

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

        Event event = Event.EventBuilder.anEvent()
            .withName("Test Event")
            .withCategory(Event.EventCategory.CLASSICAL)
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
            () -> assertEquals("Test Location", event.getLocation().getName())
        );

    }
}
