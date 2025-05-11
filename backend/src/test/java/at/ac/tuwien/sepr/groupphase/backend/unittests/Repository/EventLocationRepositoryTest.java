package at.ac.tuwien.sepr.groupphase.backend.unittests.Repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
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
public class EventLocationRepositoryTest {

    @Autowired
    private EventLocationRepository eventLocationRepository;

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
}
