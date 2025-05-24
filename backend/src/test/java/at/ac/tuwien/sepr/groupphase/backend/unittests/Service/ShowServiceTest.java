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
    @Autowired private RoomRepository roomRepository;
    @Autowired private ShowValidator showValidator;

    private ShowServiceImpl showService;

    private Event testEvent;

    private Artist testArtist;

    private Room testRoom;

    @BeforeEach
    public void setUp() {
        showService = new ShowServiceImpl(showRepository, eventRepository, artistRepository, showValidator, roomRepository);

        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Konzerthaus")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Test Street")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.THEATER)
            .build();
        eventLocationRepository.save(location);

        testRoom = Room.RoomBuilder.aRoom()
            .name("Main Room")
            .eventLocation(location)
            .build();
        roomRepository.save(testRoom);

        testEvent = Event.EventBuilder.anEvent()
            .withName("Beethoven Night")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("An evening of Beethoven")
            .withDateTime(LocalDateTime.now().plusDays(1))
            .withDuration(300)
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
        roomRepository.deleteAll();
        eventLocationRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testGetShowById_existingId_returnsShow() throws ValidationException {
        Show show = Show.ShowBuilder.aShow()
            .withName("Evening Performance")
            .withDuration(100)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        Show saved = showService.createShow(show);

        Show result = showService.getShowById(saved.getId());

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(100, result.getDuration()),
            () -> assertEquals(testEvent.getId(), result.getEvent().getId()),
            () -> assertEquals(testRoom.getId(), result.getRoom().getId()),
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
    @Transactional
    public void testGetAllShows_returnsList() throws ValidationException {
        Show show = Show.ShowBuilder.aShow()
            .withName("Matinee Concert")
            .withDuration(80)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        showService.createShow(show);

        List<Show> result = showService.getAllShows();

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals(80, result.get(0).getDuration())
        );
    }

    @Test
    @Transactional
    public void testCreateShow_validInput_savesSuccessfully() throws ValidationException {
        LocalDateTime start = testEvent.getDateTime().plusMinutes(10);

        Show newShow = Show.ShowBuilder.aShow()
            .withName("Festival Opening")
            .withDuration(120)
            .withDate(start)
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        Show saved = showService.createShow(newShow);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals(120, saved.getDuration()),
            () -> assertEquals(1, saved.getArtists().size()),
            () -> assertEquals(testEvent.getId(), saved.getEvent().getId()),
            () -> assertEquals(testRoom.getId(), saved.getRoom().getId()),
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
            .withRoom(testRoom)
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
            .withRoom(testRoom)
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
            .withRoom(testRoom)
            .build();

        assertAll(
            () -> assertThrows(ValidationException.class, () -> showService.createShow(show)),
            () -> assertEquals(0, showRepository.findAll().size())
        );
    }

    @Test
    @Transactional
    public void testCreateShow_singleShowWithinDuration_savesSuccessfully() throws ValidationException {
        Show show = Show.ShowBuilder.aShow()
            .withName("Early Performance")
            .withDuration(90)
            .withDate(LocalDateTime.now().plusDays(1).withHour(18))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        Show saved = showService.createShow(show);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals(90, saved.getDuration()),
            () -> assertEquals(testEvent.getId(), saved.getEvent().getId())
        );
    }

    @Test
    @Transactional
    public void testCreateShow_multipleShowsWithinEventDuration_savesSuccessfully() throws ValidationException {
        testEvent.setDuration(220);
        eventRepository.save(testEvent);
        LocalDateTime start = testEvent.getDateTime().plusMinutes(10);

        Show first = Show.ShowBuilder.aShow()
            .withName("Part 1")
            .withDuration(80)
            .withDate(start)
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        Show second = Show.ShowBuilder.aShow()
            .withName("Part 2")
            .withDuration(90)
            .withDate(LocalDateTime.now().plusDays(1).withHour(19))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        showService.createShow(first);
        Show saved = showService.createShow(second);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals("Part 2", saved.getName()),
            () -> assertEquals(2, showRepository.findAll().size())
        );
    }

    @Test
    @Transactional
    public void testCreateShow_exceedsEventDuration_throwsValidationException() throws ValidationException {
        testEvent.setDuration(200);
        eventRepository.save(testEvent);
        LocalDateTime start = testEvent.getDateTime().plusMinutes(10);

        Show first = Show.ShowBuilder.aShow()
            .withName("Opening")
            .withDuration(100)
            .withDate(start)
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        LocalDateTime startsecond = testEvent.getDateTime().plusMinutes(120);
        Show second = Show.ShowBuilder.aShow()
            .withName("Too Much")
            .withDuration(100)
            .withDate(startsecond)
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        showService.createShow(first);

        assertAll(
            () -> assertThrows(ValidationException.class, () -> showService.createShow(second)),
            () -> assertEquals(1, showRepository.findAll().size())
        );
    }

    @Test
    @Transactional
    public void testCreateShow_startsBeforeExistingShow_withinDuration_savesSuccessfully() throws ValidationException {
        testEvent.setDuration(280);
        eventRepository.save(testEvent);
        LocalDateTime dateLater = testEvent.getDateTime().plusMinutes(120); // z.B. 10 + 90
        LocalDateTime dateEarlier = testEvent.getDateTime().plusMinutes(10);

        Show later = Show.ShowBuilder.aShow()
            .withName("Late Show")
            .withDuration(90)
            .withDate(dateLater)
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        Show earlier = Show.ShowBuilder.aShow()
            .withName("Early Show")
            .withDuration(60)
            .withDate(dateEarlier)
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        showService.createShow(later);
        Show saved = showService.createShow(earlier);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals(2, showRepository.findAll().size())
        );
    }


    @Test
    @Transactional
    public void testCreateShow_startBeforeAndEndAfterExisting_exceedsDuration_throwsValidationException() throws ValidationException {
        testEvent.setDuration(200);
        eventRepository.save(testEvent);

        Show existing = Show.ShowBuilder.aShow()
            .withName("Anchor")
            .withDuration(90)
            .withDate(LocalDateTime.now().plusDays(1).withHour(18))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        Show tooEarlyAndLong = Show.ShowBuilder.aShow()
            .withName("Too Early")
            .withDuration(100)
            .withDate(LocalDateTime.now().plusDays(1).withHour(16))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();

        showService.createShow(existing);

        assertAll(
            () -> assertThrows(ValidationException.class, () -> showService.createShow(tooEarlyAndLong)),
            () -> assertEquals(1, showRepository.findAll().size())
        );
    }
}
