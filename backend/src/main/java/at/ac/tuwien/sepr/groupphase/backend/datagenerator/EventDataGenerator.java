package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import io.jsonwebtoken.lang.Collections;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Optional;

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
        if (!eventLocationRepository.findAll().isEmpty()) {
            LOGGER.debug("locations already generated");
        } else {
            LOGGER.debug("generating {} EventLocation entries", NUMBER_OF_LOCATIONS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_LOCATIONS_TO_GENERATE; i++) {
                long id = -(NUMBER_OF_LOCATIONS_TO_GENERATE - i);
                EventLocation eventLocation = EventLocation.EventLocationBuilder.anEventLocation()
                    .withId(id)
                    .withName("Location " + i)
                    .withType(EventLocation.LocationType.OPERA)
                    .withCountry("Country " + i)
                    .withCity("City " + i)
                    .withStreet("Street " + i)
                    .withPostalCode("PostalCode " + i)
                    .build();
                LOGGER.debug("saving event location {}", eventLocation);
                eventLocationRepository.save(eventLocation);
            }
        }

        if (!eventRepository.findAll().isEmpty()) {
            LOGGER.debug("events already generated");
        } else {
            LOGGER.debug("generating {} Event entries", NUMBER_OF_EVENTS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_EVENTS_TO_GENERATE; i++) {
                long id = -(NUMBER_OF_EVENTS_TO_GENERATE - i);
                Optional<EventLocation> eventLocation = eventLocationRepository.findById((long) -(NUMBER_OF_LOCATIONS_TO_GENERATE - (i % NUMBER_OF_LOCATIONS_TO_GENERATE)));
                if (eventLocation.isPresent()) {
                    Event event = Event.EventBuilder.anEvent()
                        .withId(id)
                        .withName("Event " + i)
                        .withCategory(Event.EventCategory.CLASSICAL)
                        .withLocation(eventLocation.get())
                        .build();
                    LOGGER.debug("saving event {}", event);
                    eventRepository.save(event);
                }
            }
        }

        if (!artistRepository.findAll().isEmpty()) {
            LOGGER.debug("artists already generated");
        } else {
            LOGGER.debug("generating {} Artist entries", NUMBER_OF_ARTISTS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_ARTISTS_TO_GENERATE; i++) {
                long id = -(NUMBER_OF_ARTISTS_TO_GENERATE - i);
                Artist artist = Artist.ArtistBuilder.anArtist()
                    .withId(id)
                    .withFirstname("Firstname " + i)
                    .withLastname("Lastname " + i)
                    .withStagename("Stagename " + i)
                    .build();
                LOGGER.debug("saving artist {}", artist);
                artistRepository.save(artist);
            }
        }

        if (!showRepository.findAll().isEmpty()) {
            LOGGER.debug("shows already generated");
        } else {
            LOGGER.debug("generating {} Show entries", NUMBER_OF_SHOWS_TO_GENERATE);
            int artistIndex = 0;
            for (int i = 0; i < NUMBER_OF_EVENTS_TO_GENERATE; i++) {
                Optional<Event> eventOpt = eventRepository.findById((long) -(NUMBER_OF_EVENTS_TO_GENERATE - i));
                if (eventOpt.isPresent()) {
                    Event event = eventOpt.get();
                    for (int j = 0; j < 2; j++) { // 2 shows per event
                        long showId = -(NUMBER_OF_SHOWS_TO_GENERATE - (i * 2 + j));
                        Optional<Artist> artist1Opt = artistRepository.findById((long) -(NUMBER_OF_ARTISTS_TO_GENERATE - artistIndex));
                        Optional<Artist> artist2Opt = artistRepository.findById((long) -(NUMBER_OF_ARTISTS_TO_GENERATE - (artistIndex + 1)));
                        if (artist1Opt.isPresent() && artist2Opt.isPresent()) {
                            Artist artist1 = artist1Opt.get();
                            Artist artist2 = artist2Opt.get();

                            Show show = Show.ShowBuilder.aShow()
                                .withId(showId)
                                .withDuration(120)
                                .withDateTime(LocalDateTime.of(2025, 6, i + 1, 12 + j * 4, 0))
                                .withEvent(event)
                                .withArtists(Collections.setOf(artist1, artist2))
                                .build();


                            artist1.setShows(Collections.setOf(show));
                            artist2.setShows(Collections.setOf(show));

                            LOGGER.debug("saving show {}", show);
                            showRepository.save(show);
                            artistRepository.save(artist1);
                            artistRepository.save(artist2);
                        }
                        artistIndex += 2;
                    }
                }
            }
        }
    }
}
