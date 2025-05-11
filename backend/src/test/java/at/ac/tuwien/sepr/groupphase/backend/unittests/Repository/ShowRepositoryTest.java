package at.ac.tuwien.sepr.groupphase.backend.unittests.Repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        Event event = Event.EventBuilder.anEvent()
            .withName("Summer Fest")
            .withCategory(Event.EventCategory.POP)
            .withLocation(location)
            .build();
        eventRepository.save(event);

        Show show = Show.ShowBuilder.aShow()
            .withDuration(150)
            .withDateTime(LocalDateTime.now())
            .withEvent(event)
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
            () -> assertEquals(1, show.getArtists().size())
        );
    }
}
