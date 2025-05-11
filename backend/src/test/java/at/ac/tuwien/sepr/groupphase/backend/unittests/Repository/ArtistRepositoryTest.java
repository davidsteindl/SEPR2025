package at.ac.tuwien.sepr.groupphase.backend.unittests.Repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    private Artist savedArtist;

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

        Event event = Event.EventBuilder.anEvent()
            .withName("Electronic Night")
            .withCategory(Event.EventCategory.ELECTRONIC)
            .withLocation(location)
            .build();
        eventRepository.save(event);

        Show show = Show.ShowBuilder.aShow()
            .withDuration(90)
            .withDateTime(LocalDateTime.now())
            .withEvent(event)
            .build();
        showRepository.save(show);

        Artist artist = Artist.ArtistBuilder.anArtist()
            .withFirstname("Anna")
            .withLastname("Smith")
            .withStagename("DJ Anna")
            .withShows(Set.of(show))
            .build();
        savedArtist = artistRepository.save(artist);
    }

    @AfterEach
    public void deleteData() {
        artistRepository.deleteAll();
        showRepository.deleteAll();
        eventRepository.deleteAll();
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
}
