package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Profile("generateData")
@Component
public class EventDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EventRepository eventRepository;
    private final EventLocationRepository eventLocationRepository;
    private final ShowRepository showRepository;
    private final ArtistRepository artistRepository;

    private static final int NUMBER_OF_EVENTS_TO_GENERATE = 5;
    private static final int NUMBER_OF_LOCATIONS_TO_GENERATE = 5;
    private static final int NUMBER_OF_SHOWS_TO_GENERATE = 10;
    private static final int NUMBER_OF_ARTISTS_TO_GENERATE = 20;

    @Autowired
    public EventDataGenerator(EventRepository eventRepository, EventLocationRepository eventLocationRepository, ShowRepository showRepository,
                              ArtistRepository artistRepository) {
        this.eventRepository = eventRepository;
        this.eventLocationRepository = eventLocationRepository;
        this.showRepository = showRepository;
        this.artistRepository = artistRepository;
    }

    @PostConstruct
    private void generateEvents() {
        List<EventLocation> eventLocations = new ArrayList<>();
        List<Event> events = new ArrayList<>();
        List<Artist> artists = new ArrayList<>();
        List<Show> shows = new ArrayList<>();

        // EventLocations
        if (eventLocationRepository.count() > 0) {
            eventLocations.addAll(eventLocationRepository.findAll());
            LOGGER.debug("locations already generated");
        } else {
            LOGGER.debug("generating {} EventLocation entries", NUMBER_OF_LOCATIONS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_LOCATIONS_TO_GENERATE; i++) {
                EventLocation eventLocation = EventLocation.EventLocationBuilder.anEventLocation()
                    .withName("Location " + i)
                    .withType(EventLocation.LocationType.OPERA)
                    .withCountry("Country " + i)
                    .withCity("City " + i)
                    .withStreet("Street " + i)
                    .withPostalCode("PostalCode " + i)
                    .build();
                LOGGER.debug("saving event location {}", eventLocation);
                eventLocations.add(eventLocation);
            }
            eventLocationRepository.saveAll(eventLocations);
        }

        // Events
        if (eventRepository.count() > 0) {
            LOGGER.debug("events already generated");
        } else {
            LOGGER.debug("generating {} Event entries", NUMBER_OF_EVENTS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_EVENTS_TO_GENERATE; i++) {
                Event event = Event.EventBuilder.anEvent()
                    .withName("Event " + i)
                    .withCategory(Event.EventCategory.CLASSICAL)
                    .withLocation(eventLocations.get(i % eventLocations.size()))
                    .build();
                LOGGER.debug("saving event {}", event);
                events.add(event);
            }
            eventRepository.saveAll(events);
        }


        // Artists
        if (artistRepository.count() > 0) {
            artists.addAll(artistRepository.findAll());
            LOGGER.debug("artists already generated");
        } else {
            LOGGER.debug("generating {} Artist entries", NUMBER_OF_ARTISTS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_ARTISTS_TO_GENERATE; i++) {
                Artist artist = Artist.ArtistBuilder.anArtist()
                    .withFirstname("Firstname " + i)
                    .withLastname("Lastname " + i)
                    .withStagename("Stagename " + i)
                    .build();
                LOGGER.debug("saving artist {}", artist);
                artists.add(artist);
            }
        }

        // Shows
        if (showRepository.count() > 0) {
            LOGGER.debug("shows already generated");
        } else {
            LOGGER.debug("generating {} Show entries", NUMBER_OF_SHOWS_TO_GENERATE);
            int artistIndex = 0;
            List<Artist> artistsWithShows = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_SHOWS_TO_GENERATE; i++) {
                for (int j = 0; j < 2; j++) { // 2 shows per event
                    Artist artist1 = artists.get(artistIndex % artists.size());
                    Artist artist2 = artists.get((artistIndex + 1) % artists.size());

                    Show show = Show.ShowBuilder.aShow()
                        .withDuration(120)
                        .withDateTime(LocalDateTime.of(2025, 6, i + 1, 12 + j * 4, 0))
                        .withEvent(events.get(i % events.size()))
                        .withArtists(Set.of(artist1, artist2))
                        .build();

                    if (artist1.getShows() == null) {
                        artist1.setShows(new HashSet<>());
                    }
                    artist1.getShows().add(show);

                    if (artist2.getShows() == null) {
                        artist2.setShows(new HashSet<>());
                    }
                    artist2.getShows().add(show);

                    LOGGER.debug("saving show {}", show);
                    shows.add(show);
                    artistsWithShows.add(artist1);
                    artistsWithShows.add(artist2);
                }
                artistIndex += 2;
            }
            showRepository.saveAll(shows);
            artistRepository.saveAll(artistsWithShows);
        }
    }
}