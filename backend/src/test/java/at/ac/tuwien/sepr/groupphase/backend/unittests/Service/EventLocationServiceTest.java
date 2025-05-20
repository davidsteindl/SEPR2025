package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EventLocationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class EventLocationServiceTest {

  @Autowired
  private EventLocationRepository eventLocationRepository;

  private EventLocationServiceImpl eventLocationService;

  private ShowRepository showRepository;

  private EventLocation testLocation;

  @BeforeEach
  public void setUp() {
    eventLocationService = new EventLocationServiceImpl(eventLocationRepository, showRepository);

    testLocation = EventLocation.EventLocationBuilder.anEventLocation()
        .withName("Wiener Staatsoper")
        .withCountry("Austria")
        .withCity("Vienna")
        .withStreet("Opernring 2")
        .withPostalCode("1010")
        .withType(EventLocation.LocationType.OPERA)
        .build();

    eventLocationRepository.save(testLocation);
  }

  @AfterEach
  public void deleteData() {
    eventLocationRepository.deleteAll();
  }

  @Test
  public void testGetEventLocationById_existingId_returnsLocation() {
    EventLocation result = eventLocationService.getEventLocationById(testLocation.getId());

    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals("Wiener Staatsoper", result.getName()),
        () -> assertEquals("Austria", result.getCountry()),
        () -> assertEquals("Vienna", result.getCity()),
        () -> assertEquals("Opernring 2", result.getStreet()),
        () -> assertEquals("1010", result.getPostalCode()),
        () -> assertEquals(EventLocation.LocationType.OPERA, result.getType())
    );
  }

  @Test
  public void testGetEventLocationById_nonExistingId_returnsNull() {
    EventLocation result = eventLocationService.getEventLocationById(999L);

    assertAll(
        () -> assertNull(result),
        () -> assertEquals(1, eventLocationRepository.findAll().size())
    );
  }

  @Test
  public void testGetAllEventLocations_returnsList() {
    List<EventLocation> result = eventLocationService.getAllEventLocations();

    assertAll(
        () -> assertEquals(1, result.size()),
        () -> assertEquals("Wiener Staatsoper", result.getFirst().getName())
    );
  }

  @Test
  public void testCreateEventLocation_validLocation_savesSuccessfully() {
    EventLocation newLocation = EventLocation.EventLocationBuilder.anEventLocation()
        .withName("Theater")
        .withCountry("Austria")
        .withCity("Vienna")
        .withStreet("Test Street")
        .withPostalCode("1030")
        .withType(EventLocation.LocationType.THEATER)
        .build();

    EventLocation saved = eventLocationService.createEventLocation(newLocation);

    assertAll(
        () -> assertNotNull(saved.getId()),
        () -> assertEquals("Theater", saved.getName()),
        () -> assertEquals(2, eventLocationRepository.findAll().size())
    );
  }
}
