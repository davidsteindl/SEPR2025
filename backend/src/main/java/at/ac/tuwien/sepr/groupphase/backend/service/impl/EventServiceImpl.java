package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EventServiceImpl implements EventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventRepository eventRepository;
    private final EventLocationRepository eventLocationRepository;
    private final ArtistRepository artistRepository;
    private final ShowRepository showRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, EventLocationRepository eventLocationRepository,
                            ArtistRepository artistRepository, ShowRepository showRepository) {
        this.eventRepository = eventRepository;
        this.eventLocationRepository = eventLocationRepository;
        this.artistRepository = artistRepository;
        this.showRepository = showRepository;
    }

    @Override
    public Event getEventById(Long id) {
        LOGGER.info("Find event with id {}", id);
        return eventRepository.findById(id).orElse(null);
    }

    @Override
    public List<Event> getAllEvents() {
        LOGGER.info("Get all events");
        return eventRepository.findAll();
    }

    @Override
    public Event createEvent(Event event) {
        LOGGER.info("Save event {}", event);
        if (event.getLocation() != null) {
            EventLocation location = eventLocationRepository.findById(event.getLocation().getId())
                .orElseThrow(() -> new ValidationException("Event location not found"));
            event.setLocation(location);
        } else {
            throw new ValidationException("Event location must not be null");
        }
        return eventRepository.save(event);
    }

    @Override
    public EventLocation getEventLocationById(Long eventId) {
        LOGGER.info("Find event location with id {}", eventId);
        return eventLocationRepository.findById(eventId).orElse(null);
    }

    @Override
    public List<EventLocation> getAllEventLocations() {
        LOGGER.info("Get all event locations");
        return eventLocationRepository.findAll();
    }

    @Override
    public EventLocation createEventLocation(EventLocation eventLocation) {
        LOGGER.info("Save event location {}", eventLocation);
        return eventLocationRepository.save(eventLocation);
    }

    @Override
    public Artist getArtistById(Long id) {
        LOGGER.info("Find artist with id {}", id);
        return artistRepository.findById(id).orElse(null);
    }

    @Override
    public List<Artist> getAllArtists() {
        LOGGER.info("Get all artists");
        return artistRepository.findAll();
    }

    @Override
    public Artist createArtist(Artist artist) {
        LOGGER.info("Save artist {}", artist);
        Set<Show> existingShows = new HashSet<>();
        if (artist.getShows() != null) {
            for (Show show : artist.getShows()) {
                Show existingShow = showRepository.findById(show.getId())
                    .orElseThrow(() -> new ValidationException("Show with id " + show.getId() + " not found"));
                existingShows.add(existingShow);
            }
        }
        artist.setShows(existingShows);
        return artistRepository.save(artist);
    }

    @Override
    public Show getShowById(Long id) {
        LOGGER.info("Find show with id {}", id);
        return showRepository.findById(id).orElse(null);
    }

    @Override
    public List<Show> getAllShows() {
        LOGGER.info("Get all shows");
        return showRepository.findAll();
    }

    @Override
    public Show createShow(Show show) {
        LOGGER.info("Save show {}", show);
        return showRepository.save(show);
    }
}
