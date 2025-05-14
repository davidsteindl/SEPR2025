package at.ac.tuwien.sepr.groupphase.backend.unittests.Repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class EventLocationRepositoryTest {

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        EventLocation eventLocation = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Opera House")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Main Street")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.OPERA)
            .build();

        eventLocationRepository.save(eventLocation);
    }

    @AfterEach
    public void deleteData() {
        eventLocationRepository.deleteAll();
    }

    @Test
    public void findAllAndGetSize1() {
        assertEquals(1, eventLocationRepository.findAll().size());
    }

    @Test
    public void findById() {
        EventLocation eventLocation = eventLocationRepository.findAll().getFirst();
        assertAll(
            () -> assertNotNull(eventLocationRepository.findById(eventLocation.getId())),
            () -> assertNotNull(eventLocation.getId()),
            () -> assertEquals("Opera House", eventLocation.getName()),
            () -> assertEquals("Austria", eventLocation.getCountry()),
            () -> assertEquals("Vienna", eventLocation.getCity()),
            () -> assertEquals("Main Street", eventLocation.getStreet()),
            () -> assertEquals("1010", eventLocation.getPostalCode()),
            () -> assertEquals(EventLocation.LocationType.OPERA, eventLocation.getType())
        );
    }

    @Test
    public void testSave_withNullName_throwsException() {
        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName(null)
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Street")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.CLUB)
            .build();

        assertThrows(Exception.class, () -> eventLocationRepository.saveAndFlush(location));
        entityManager.clear();
    }

    @Test
    public void testSave_withNullType_throwsException() {
        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("NoType")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Street")
            .withPostalCode("1010")
            .withType(null)
            .build();

        assertThrows(Exception.class, () -> eventLocationRepository.saveAndFlush(location));
        entityManager.clear();
    }

    @Test
    public void testSave_withNullCountry_throwsException() {
        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("No Country")
            .withCountry(null)
            .withCity("Vienna")
            .withStreet("Street")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.THEATER)
            .build();

        assertThrows(Exception.class, () -> eventLocationRepository.saveAndFlush(location));
        entityManager.clear();
    }

    @Test
    public void testSave_withNullCity_throwsException() {
        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("No City")
            .withCountry("Austria")
            .withCity(null)
            .withStreet("Street")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.HALL)
            .build();

        assertThrows(Exception.class, () -> eventLocationRepository.saveAndFlush(location));
        entityManager.clear();
    }

    @Test
    public void testSave_withNullStreet_throwsException() {
        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("No Street")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet(null)
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.STADIUM)
            .build();

        assertThrows(Exception.class, () -> eventLocationRepository.saveAndFlush(location));
        entityManager.clear();
    }

    @Test
    public void testSave_withNullPostalCode_throwsException() {
        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("No Postal")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Street")
            .withPostalCode(null)
            .withType(EventLocation.LocationType.HALL)
            .build();

        assertThrows(Exception.class, () -> eventLocationRepository.saveAndFlush(location));
        entityManager.clear();
    }
}