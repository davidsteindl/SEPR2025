package at.ac.tuwien.sepr.groupphase.backend.unittests.Repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class ShowRepositoryTest {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    private Room testRoom;

    @BeforeEach
    public void setUp() {
        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Festival Ground")
            .withCountry("Austria")
            .withCity("Salzburg")
            .withStreet("Festival Street")
            .withPostalCode("5020")
            .withType(EventLocation.LocationType.FESTIVAL_GROUND)
            .build();
        eventLocationRepository.save(location);

        testRoom = Room.RoomBuilder.aRoom()
            .name("Main Room")
            .eventLocation(location)
            .build();
        roomRepository.save(testRoom);

        LocalDateTime eventStart = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES);
        Event event = Event.EventBuilder.anEvent()
            .withName("Summer Fest")
            .withCategory(Event.EventCategory.POP)
            .withDescription("A summer celebration of music.")
            .withDateTime(eventStart)
            .withDuration(300)
            .withLocation(location)
            .build();
        eventRepository.save(event);

        Show show = Show.ShowBuilder.aShow()
            .withName("Evening Show")
            .withDuration(150)
            .withDate(eventStart.plusHours(1))
            .withEvent(event)
            .withRoom(testRoom)
            .build();
        showRepository.save(show);

        Artist artist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Max")
            .withLastname("Mustermann")
            .withStagename("MaxStar")
            .build();
        artistRepository.save(artist);

        artist.setShows(new HashSet<>(List.of(show)));
        artistRepository.save(artist);

        show.setArtists(new HashSet<>(List.of(artist)));
        showRepository.save(show);
    }

    @AfterEach
    public void deleteData() {
        artistRepository.deleteAll();
        showRepository.deleteAll();
        eventRepository.deleteAll();
        roomRepository.deleteAll();
        eventLocationRepository.deleteAll();
    }

    @Test
    public void findAllAndGetSize1() {
        assertEquals(1, showRepository.findAll().size());
    }

    @Test
    public void findById() {
        Show show = showRepository.findAll().getFirst();
        assertAll(
            () -> assertNotNull(showRepository.findById(show.getId())),
            () -> assertNotNull(show.getId()),
            () -> assertEquals(150, show.getDuration()),
            () -> assertEquals("Summer Fest", show.getEvent().getName()),
            () -> assertEquals("Festival Ground", show.getEvent().getLocation().getName()),
            () -> assertEquals("Evening Show", show.getName()),
            () -> assertEquals("Main Room", show.getRoom().getName()),
            () -> assertEquals(1, show.getArtists().size())
        );
    }

    @Test
    public void testSaveShow_withTooSmallDuration_throwsException() {
        Event event = eventRepository.findAll().getFirst();

        LocalDateTime showDateSmall = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES);
        Show show = Show.ShowBuilder.aShow()
            .withName("Short Show")
            .withDuration(5)
            .withDate(showDateSmall)
            .withEvent(event)
            .withRoom(testRoom)
            .build();

        assertThrows(Exception.class, () -> showRepository.saveAndFlush(show));
        entityManager.clear();
    }

    @Test
    public void testSaveShow_withTooLargeDuration_throwsException() {
        Event event = eventRepository.findAll().getFirst();

        LocalDateTime showDateLarge = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES);
        Show show = Show.ShowBuilder.aShow()
            .withName("Long Show")
            .withDuration(1000)
            .withDate(showDateLarge)
            .withEvent(event)
            .withRoom(testRoom)
            .build();

        assertThrows(Exception.class, () -> showRepository.saveAndFlush(show));
        entityManager.clear();
    }

    @Test
    public void testSaveShow_withNullDate_throwsException() {
        Event event = eventRepository.findAll().getFirst();

        Show show = Show.ShowBuilder.aShow()
            .withName("No Date Show")
            .withDuration(100)
            .withDate(null)
            .withEvent(event)
            .withRoom(testRoom)
            .build();

        assertThrows(Exception.class, () -> showRepository.saveAndFlush(show));
        entityManager.clear();
    }

    @Test
    public void testSaveShow_withNullEvent_throwsException() {
        Show show = Show.ShowBuilder.aShow()
            .withName("No Event Show")
            .withDuration(100)
            .withDate(LocalDateTime.now())
            .withEvent(null)
            .withRoom(testRoom)
            .build();

        assertThrows(Exception.class, () -> showRepository.saveAndFlush(show));
        entityManager.clear();
    }

    @Test
    public void testSaveShow_withValidFields_savesSuccessfully() {
        Event event = eventRepository.findAll().getFirst();

        LocalDateTime showDateValid = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES);
        Show show = Show.ShowBuilder.aShow()
            .withName("Main Act")
            .withDuration(120)
            .withDate(showDateValid)
            .withEvent(event)
            .withRoom(testRoom)
            .build();

        Show saved = showRepository.save(show);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals("Main Act", saved.getName()),
            () -> assertEquals(120, saved.getDuration()),
            () -> assertEquals(event.getId(), saved.getEvent().getId()),
            () -> assertEquals(testRoom.getId(), saved.getRoom().getId())
        );
    }

    @Test
    public void testFindEventsByArtistId_returnsExpectedEvent() {
        Artist artist = artistRepository.findAll().getFirst();
        Page<Event> result = showRepository.findEventsByArtistId(artist.getId(), PageRequest.of(0, 10));

        assertAll(
            () -> assertEquals(1, result.getTotalElements()),
            () -> assertEquals("Summer Fest", result.getContent().getFirst().getName())
        );
    }


}