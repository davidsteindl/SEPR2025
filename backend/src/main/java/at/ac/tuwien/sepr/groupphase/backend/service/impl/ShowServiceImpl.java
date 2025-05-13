package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        return showRepository.findAllWithArtists();
    }

    @Override
    @Transactional
    public Show createShow(Show show) throws ValidationException {
        LOGGER.info("Save show {}", show);

        if (show.getEvent() == null || show.getEvent().getId() == null) {
            throw new ValidationException("Event ID must not be null", List.of("Event ID is null"));
        }

        Event event = eventRepository.findById(show.getEvent().getId())
            .orElseThrow(() -> new ValidationException("Event not found", List.of("Event not found")));
        show.setEvent(event);

        if (show.getArtists() == null || show.getArtists().isEmpty()) {
            throw new ValidationException("Artist list must not be empty", List.of("Artist list is empty"));
        }

        Set<Artist> validatedArtists = show.getArtists().stream().map(a -> {
            if (a.getId() == null) {
                throw new RuntimeException("Artist ID must not be null");
            }
            return artistRepository.findById(a.getId())
                .orElseThrow(() -> new RuntimeException("Artist not found"));
        }).collect(Collectors.toSet());

        show.setArtists(validatedArtists);

        return showRepository.save(show);
    }

}
