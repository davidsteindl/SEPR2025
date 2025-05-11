package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShowServiceImpl implements ShowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ShowRepository showRepository;
    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;

    @Autowired
    public ShowServiceImpl(ShowRepository showRepository, EventRepository eventRepository, ArtistRepository artistRepository) {
        this.showRepository = showRepository;
        this.eventRepository = eventRepository;
        this.artistRepository = artistRepository;
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
    public Show createShow(Show show) throws ValidationException {
        LOGGER.info("Save show {}", show);
        if (show.getEvent().getId() == null) {
            LOGGER.error("Event ID must not be null");
            throw new ValidationException("Event ID must not be null", List.of("Event ID must not be null"));
        } else {
            Event event = eventRepository.findById(show.getEvent().getId())
                .orElseThrow(() -> new ValidationException("Event ID must not be null", List.of("Event ID must not be null")));
            show.setEvent(event);
        }

        if (show.getArtists() == null || show.getArtists().isEmpty()) {
            LOGGER.error("Show must have at least one artist");
            throw new ValidationException("Show must have at least one artist", List.of("Artist list is empty"));
        } else {
            Set<Artist> existingArtists = new HashSet<>();
            for (Artist artist : show.getArtists()) {
                if (artist.getId() == null) {
                    LOGGER.error("Artist ID must not be null");
                    throw new ValidationException("Artist ID must not be null", List.of("Artist ID must not be null"));
                }
                Artist existingArtist = artistRepository.findById(artist.getId())
                    .orElseThrow(() -> new ValidationException("Artist not found", List.of("Artist not found")));
                existingArtists.add(existingArtist);
            }
            show.setArtists(existingArtists);
        }
        return showRepository.save(show);
    }
}
