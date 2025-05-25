package at.ac.tuwien.sepr.groupphase.backend.unittests.Repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class ArtistRepositoryTest {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Club X")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Party Street")
            .withPostalCode("1020")
            .withType(EventLocation.LocationType.CLUB)
            .build();
        eventLocationRepository.save(location);

        Room room = Room.RoomBuilder.aRoom()
            .name("Main Room")
            .eventLocation(location)
            .build();
        roomRepository.save(room);

        LocalDateTime eventStart = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES);
            Event event = Event.EventBuilder.anEvent()
            .withName("Electronic Night")
            .withCategory(Event.EventCategory.ELECTRONIC)
            .withDescription("An electrifying night with the best DJs.")
            .withDateTime(eventStart)
            .withDuration(180)
            .withLocation(location)
            .build();
        eventRepository.save(event);

        Show show = Show.ShowBuilder.aShow()
            .withName("Opening Act")
            .withDuration(90)
            .withDate(eventStart.plusMinutes(10))
            .withEvent(event)
            .withRoom(room)
            .build();
        showRepository.save(show);

        Artist artist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Anna")
            .withLastname("Smith")
            .withStagename("DJ Anna")
            .withShows(Set.of(show))
            .build();
        artistRepository.save(artist);
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
        assertEquals(1, artistRepository.findAll().size());
    }

    @Test
    public void findById() {
        Artist artist = artistRepository.findAll().getFirst();
        assertAll(
            () -> assertNotNull(artistRepository.findById(artist.getId())),
            () -> assertNotNull(artist.getId()),
            () -> assertEquals("Anna", artist.getFirstname()),
            () -> assertEquals("Smith", artist.getLastname()),
            () -> assertEquals("DJ Anna", artist.getStagename()),
            () -> assertEquals(1, artist.getShows().size())
        );
    }

    @Test
    public void testSaveArtist_withNullFirstname_savesSuccessfully() {
        Artist artist = Artist.ArtistBuilder.anArtist()
            .withFirstname(null)
            .withLastname("Doe")
            .withStagename("Stage")
            .withShows(null)
            .build();

        Artist saved = artistRepository.save(artist);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals("Doe", saved.getLastname()),
            () -> assertEquals("Stage", saved.getStagename())
        );
    }

    @Test
    public void testSaveArtist_withNullLastname_savesSuccessfully() {
        Artist artist = Artist.ArtistBuilder.anArtist()
            .withFirstname("John")
            .withLastname(null)
            .withStagename("Stage")
            .withShows(null)
            .build();

        Artist saved = artistRepository.save(artist);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals("John", saved.getFirstname()),
            () -> assertEquals("Stage", saved.getStagename())
        );
    }

    @Test
    public void testSaveArtist_withNullStagename_savesSuccessfully() {
        Artist artist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Jane")
            .withLastname("Doe")
            .withStagename(null)
            .withShows(null)
            .build();

        Artist saved = artistRepository.save(artist);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals("Jane", saved.getFirstname()),
            () -> assertEquals("Doe", saved.getLastname())
        );
    }
}
