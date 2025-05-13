package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ShowServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class ShowServiceTest {

    @Autowired private ShowRepository showRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private EventLocationRepository eventLocationRepository;

    private ShowServiceImpl showService;

    private Event testEvent;
    private Artist testArtist;

    @BeforeEach
    public void setUp() {
        showService = new ShowServiceImpl(showRepository, eventRepository, artistRepository);

        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Konzerthaus")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Test Street")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.THEATER)
            .build();
        eventLocationRepository.save(location);

        testEvent = Event.EventBuilder.anEvent()
            .withName("Beethoven Night")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withLocation(location)
            .build();
        eventRepository.save(testEvent);

        testArtist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Ludwig")
            .withLastname("Beethoven")
            .withStagename("LB")
            .withShows(null)
            .build();
        artistRepository.save(testArtist);
    }

    @AfterEach
    public void tearDown() {
        showRepository.deleteAll();
        artistRepository.deleteAll();
        eventRepository.deleteAll();
        eventLocationRepository.deleteAll();
    }

    @Test
    public void testGetShowById_existingId_returnsShow() {
        Show show = Show.ShowBuilder.aShow()
            .withDuration(100)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .build();
        showRepository.save(show);

        Show result = showService.getShowById(show.getId());

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(100, result.getDuration()),
            () -> assertEquals(testEvent.getId(), result.getEvent().getId()),
            () -> assertEquals(1, result.getArtists().size())
        );
    }

    @Test
    public void testGetShowById_nonExisting_returnsNull() {
        Show result = showService.getShowById(999L);

        assertAll(
            () -> assertNull(result),
            () -> assertEquals(0, showRepository.findAll().size())
        );
    }

    @Test
    public void testGetAllShows_returnsList() {
        Show show = Show.ShowBuilder.aShow()
            .withDuration(80)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .build();
        showRepository.save(show);

        List<Show> result = showService.getAllShows();

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals(80, result.get(0).getDuration())
        );
    }

    @Test
    public void testCreateShow_validInput_savesSuccessfully() throws ValidationException {
        Show newShow = Show.ShowBuilder.aShow()
            .withDuration(120)
            .withDate(LocalDateTime.now().plusDays(2))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .build();

        Show saved = showService.createShow(newShow);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals(120, saved.getDuration()),
            () -> assertEquals(1, saved.getArtists().size()),
            () -> assertEquals(testEvent.getId(), saved.getEvent().getId()),
            () -> assertEquals(1, showRepository.findAll().size())
        );
    }

    @Test
    public void testCreateShow_nullEvent_throwsValidationException() {
        Show show = Show.ShowBuilder.aShow()
            .withDuration(60)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(null)
            .withArtists(Set.of(testArtist))
            .build();

        assertAll(
            () -> assertThrows(NullPointerException.class, () -> showService.createShow(show)),
            () -> assertEquals(0, showRepository.findAll().size())
        );
    }

    @Test
    public void testCreateShow_eventNotInDb_throwsValidationException() {
        Event fakeEvent = Event.EventBuilder.anEvent()
            .withName("Ghost Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withLocation(testEvent.getLocation())
            .build();
        fakeEvent.setId(999L);

        Show show = Show.ShowBuilder.aShow()
            .withDuration(90)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(fakeEvent)
            .withArtists(Set.of(testArtist))
            .build();

        assertAll(
            () -> assertThrows(ValidationException.class, () -> showService.createShow(show)),
            () -> assertEquals(0, showRepository.findAll().size())
        );
    }

    @Test
    public void testCreateShow_noArtists_throwsValidationException() {
        Show show = Show.ShowBuilder.aShow()
            .withDuration(60)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withArtists(null)
            .build();

        assertAll(
            () -> assertThrows(ValidationException.class, () -> showService.createShow(show)),
            () -> assertEquals(0, showRepository.findAll().size())
        );
    }

    @Test
    public void testCreateShow_artistNotInDb_throwsValidationException() {
        Artist ghostArtist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Ghost")
            .withLastname("Artist")
            .withStagename("Phantom")
            .build();
        ghostArtist.setId(999L);

        Show show = Show.ShowBuilder.aShow()
            .withDuration(70)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withArtists(Set.of(ghostArtist))
            .build();

        assertAll(
            () -> assertThrows(ValidationException.class, () -> showService.createShow(show)),
            () -> assertEquals(0, showRepository.findAll().size())
        );
    }
}
