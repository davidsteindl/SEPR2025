package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.service.validators.ShowValidator;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ShowServiceImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ShowServiceTest {

    @Autowired private ShowRepository showRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private EventLocationRepository eventLocationRepository;

    private ShowServiceImpl showService;

    private Event testEvent;
    private Artist testArtist;

    @Autowired
    private ShowValidator showValidator;

    @BeforeEach
    public void setUp() {
        showService = new ShowServiceImpl(showRepository, eventRepository, artistRepository, showValidator);

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
            .withDescription("An evening of Beethoven")
            .withDuration(180)
            .withLocation(location)
            .build();
        eventRepository.save(testEvent);

        testArtist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Ludwig")
            .withLastname("Beethoven")
            .withStagename("LB")
            .withShows(null)
            .build();
        testArtist = artistRepository.save(testArtist);
    }

    @AfterEach
    public void deleteData() {
        List<Artist> allArtists = artistRepository.findAllWithShows();

        for (Artist artist : allArtists) {
            artist.getShows().clear();
            artistRepository.save(artist);
        }

        showRepository.deleteAll();
        artistRepository.deleteAll();
        eventRepository.deleteAll();
        eventLocationRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testGetShowById_existingId_returnsShow() throws ValidationException {
        System.out.println("testArtist ID: " + testArtist.getId());
        Show show = Show.ShowBuilder.aShow()
            .withName("Evening Performance")
            .withDuration(100)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .build();

        Show saved = showService.createShow(show);

        Show result = showService.getShowById(saved.getId());
        System.out.println("Artists in result: " + result.getArtists());
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
    public void testGetAllShows_returnsList() throws ValidationException {
        Show show = Show.ShowBuilder.aShow()
            .withName("Matinee Concert")
            .withDuration(80)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .build();

        showService.createShow(show);

        List<Show> result = showService.getAllShows();

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals(80, result.get(0).getDuration())
        );
    }

    @Test
    public void testCreateShow_validInput_savesSuccessfully() throws ValidationException {
        Show newShow = Show.ShowBuilder.aShow()
            .withName("Festival Opening")
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
            .withName("Orchestra Only")
            .withDuration(60)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(null)
            .withArtists(Set.of(testArtist))
            .build();

        assertAll(
            () -> assertThrows(ValidationException.class, () -> showService.createShow(show)),
            () -> assertEquals(0, showRepository.findAll().size())
        );
    }

    @Test
    public void testCreateShow_eventNotInDb_throwsValidationException() {
        Event fakeEvent = Event.EventBuilder.anEvent()
            .withName("Ghost Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("Non-existing event")
            .withDuration(100)
            .withLocation(testEvent.getLocation())
            .build();
        fakeEvent.setId(999L);

        Show show = Show.ShowBuilder.aShow()
            .withName("Ghost Show")
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
            .withName("Solo Performance")
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
            .withName("Phantom Session")
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

    @Test
    @Transactional
    public void testGetPagedShowsForEvent_returnsCorrectPage() throws ValidationException {
        for (int i = 0; i < 7; i++) {
            Show show = Show.ShowBuilder.aShow()
                .withName("Show " + i)
                .withDuration(60 + i)
                .withDate(LocalDateTime.now().plusDays(i))
                .withEvent(testEvent)
                .withArtists(Set.of(testArtist))
                .build();
            showService.createShow(show);
        }

        var pageable = org.springframework.data.domain.PageRequest.of(0, 5);
        var resultPage = showService.getPagedShowsForEvent(testEvent.getId(), pageable);

        assertAll(
            () -> assertNotNull(resultPage),
            () -> assertEquals(5, resultPage.getContent().size()),
            () -> assertEquals(2, resultPage.getTotalPages()),
            () -> assertEquals("Show 0", resultPage.getContent().get(0).getName())
        );
    }
}