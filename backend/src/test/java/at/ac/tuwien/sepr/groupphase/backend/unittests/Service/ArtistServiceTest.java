package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ArtistServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class ArtistServiceTest {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    private ArtistServiceImpl artistService;

    private Show testShow;

    @BeforeEach
    public void setUp() {
        artistService = new ArtistServiceImpl(artistRepository, showRepository);

        EventLocation testLocation = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Wiener Konzerthaus")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Test Street")
            .withPostalCode("1030")
            .withType(EventLocation.LocationType.THEATER)
            .build();
        eventLocationRepository.save(testLocation);

        Event testEvent = Event.EventBuilder.anEvent()
            .withName("Test Concert")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withLocation(testLocation)
            .withDuration(800)
            .withDescription("A beautiful classical concert.")
            .build();
        eventRepository.save(testEvent);

        testShow = Show.ShowBuilder.aShow()
            .withName("Test Show")
            .withDuration(90)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .build();
        showRepository.save(testShow);

        Artist artist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Max")
            .withLastname("Mustermann")
            .withStagename("MM")
            .withShows(Set.of(testShow))
            .build();

        artistRepository.save(artist);
    }


    @AfterEach
    public void deleteData() {
        artistRepository.deleteAll();
        showRepository.deleteAll();
        eventRepository.deleteAll();
        eventLocationRepository.deleteAll();
    }

    @Test
    public void testGetArtistById_existingId_returnsArtist() {
        Artist artist = artistRepository.findAll().getFirst();
        Artist result = artistService.getArtistById(artist.getId());

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals("Max", result.getFirstname()),
            () -> assertEquals("Mustermann", result.getLastname()),
            () -> assertEquals("MM", result.getStagename()),
            () -> assertEquals(1, result.getShows().size())
        );
    }

    @Test
    public void testGetArtistById_nonExistingId_returnsNull() {
        Artist result = artistService.getArtistById(999L);

        assertAll(
            () -> assertNull(result),
            () -> assertEquals(1, artistRepository.findAll().size())
        );
    }

    @Test
    public void testGetAllArtists_returnsList() {
        List<Artist> result = artistService.getAllArtists();

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals("MM", result.getFirst().getStagename())
        );
    }

    @Test
    public void testCreateArtist_validArtist_savesSuccessfully() throws ValidationException {
        Artist newArtist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Justin")
            .withLastname("Bieber")
            .withStagename("JB")
            .withShows(Set.of(testShow))
            .build();

        Artist saved = artistService.createArtist(newArtist);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals("Justin", saved.getFirstname()),
            () -> assertEquals("Bieber", saved.getLastname()),
            () -> assertEquals("JB", saved.getStagename()),
            () -> assertEquals(1, saved.getShows().size()),
            () -> assertEquals(2, artistRepository.findAll().size())
        );
    }

    @Test
    public void testCreateArtist_withNonExistingShow_throwsValidationException() {
        Show fakeShow = Show.ShowBuilder.aShow()
            .withDate(LocalDateTime.now().plusDays(5))
            .build();
        fakeShow.setId(999L);

        Artist newArtist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Fake")
            .withLastname("Performer")
            .withStagename("Faker")
            .withShows(Set.of(fakeShow))
            .build();

        assertAll(
            () -> assertThrows(ValidationException.class, () -> artistService.createArtist(newArtist)),
            () -> assertEquals(1, artistRepository.findAll().size())
        );
    }

    @Test
    public void testCreateArtist_withNoShows_savesSuccessfully() throws ValidationException {
        Artist soloArtist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Solo")
            .withLastname("Artist")
            .withStagename("Tester")
            .withShows(null)
            .build();

        Artist saved = artistService.createArtist(soloArtist);

        assertAll(
            () -> assertNotNull(saved.getId()),
            () -> assertEquals("Solo", saved.getFirstname()),
            () -> assertEquals("Artist", saved.getLastname()),
            () -> assertEquals("Tester", saved.getStagename()),
            () -> assertTrue(saved.getShows().isEmpty()),
            () -> assertEquals(2, artistRepository.findAll().size())
        );
    }
}
