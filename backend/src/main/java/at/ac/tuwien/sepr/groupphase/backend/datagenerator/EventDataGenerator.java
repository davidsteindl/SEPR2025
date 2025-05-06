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
        // EventLocations
        if (!eventLocationRepository.findAll().isEmpty()) {
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
                eventLocationRepository.save(eventLocation);
            }
        }

        // Events
        if (!eventRepository.findAll().isEmpty()) {
            LOGGER.debug("events already generated");
        } else {
            LOGGER.debug("generating {} Event entries", NUMBER_OF_EVENTS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_EVENTS_TO_GENERATE; i++) {
                String locationName = "Location " + (i % NUMBER_OF_LOCATIONS_TO_GENERATE);
                Optional<EventLocation> eventLocationOpt = eventLocationRepository.findByName(locationName);
                if (eventLocationOpt.isPresent()) {
                    Event event = Event.EventBuilder.anEvent()
                        .withName("Event " + i)
                        .withCategory(Event.EventCategory.CLASSICAL)
                        .withLocation(eventLocationOpt.get())
                        .build();
                    LOGGER.debug("saving event {}", event);
                    eventRepository.save(event);
                } else {
                    LOGGER.warn("EventLocation '{}' not found!", locationName);
                }
            }
        }

        // Artists
        if (!artistRepository.findAll().isEmpty()) {
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
                artistRepository.save(artist);
            }
        }

        // Shows
        if (!showRepository.findAll().isEmpty()) {
            LOGGER.debug("shows already generated");
        } else {
            LOGGER.debug("generating {} Show entries", NUMBER_OF_SHOWS_TO_GENERATE);
            int artistIndex = 0;
            for (int i = 0; i < NUMBER_OF_EVENTS_TO_GENERATE; i++) {
                String eventName = "Event " + i;
                Optional<Event> eventOpt = eventRepository.findByName(eventName);
                if (eventOpt.isPresent()) {
                    Event event = eventOpt.get();
                    for (int j = 0; j < 2; j++) { // 2 shows per event
                        String artist1StageName = "Stagename " + artistIndex;
                        String artist2StageName = "Stagename " + (artistIndex + 1);
                        Optional<Artist> artist1Opt = artistRepository.findByStagename(artist1StageName);
                        Optional<Artist> artist2Opt = artistRepository.findByStagename(artist2StageName);
                        if (artist1Opt.isPresent() && artist2Opt.isPresent()) {
                            Artist artist1 = artist1Opt.get();
                            Artist artist2 = artist2Opt.get();

                            Show show = Show.ShowBuilder.aShow()
                                .withDuration(120)
                                .withDateTime(LocalDateTime.of(2025, 6, i + 1, 12 + j * 4, 0))
                                .withEvent(event)
                                .withArtists(Set.of(artist1, artist2))
                                .build();

                            artist1.setShows(Set.of(show));
                            artist2.setShows(Set.of(show));

                            LOGGER.debug("saving show {}", show);
                            showRepository.save(show);
                            artistRepository.save(artist1);
                            artistRepository.save(artist2);
                        } else {
                            LOGGER.warn("Artists '{}' and/or '{}' not found!", artist1StageName, artist2StageName);
                        }
                        artistIndex += 2;
                    }
                } else {
                    LOGGER.warn("Event '{}' not found!", eventName);
                }
            }
        }
    }
}