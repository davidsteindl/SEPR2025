package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.ShowValidator;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import at.ac.tuwien.sepr.groupphase.backend.util.EntitySyncUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShowServiceImpl implements ShowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ShowRepository showRepository;
    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;
    private final ShowValidator showValidator;
    private final RoomRepository roomRepository;

    @Autowired
    public ShowServiceImpl(ShowRepository showRepository,
                           EventRepository eventRepository,
                           ArtistRepository artistRepository,
                           ShowValidator showValidator,
                           RoomRepository roomRepository) {
        this.showRepository = showRepository;
        this.eventRepository = eventRepository;
        this.artistRepository = artistRepository;
        this.showValidator = showValidator;
        this.roomRepository = roomRepository;
    }

    @Override
    public Show getShowById(Long id) {
        return showRepository.findDetailedById(id)
            .orElseThrow(() -> new EntityNotFoundException("Show not found"));
    }

    @Override
    public List<Show> getAllShows() {
        LOGGER.debug("Get all shows");
        return showRepository.findAllWithArtists();
    }

    @Override
    @Transactional
    public Show createShow(Show show) throws ValidationException {
        LOGGER.debug("Saving new show with name '{}'", show.getName());

        showValidator.validateForCreate(show);

        Event event = eventRepository.findById(show.getEvent().getId()).get();
        Room room = roomRepository.findById(show.getRoom().getId()).get();

        Set<Artist> artists = show.getArtists().stream()
            .map(a -> artistRepository.findByIdWithShows(a.getId()).get())
            .collect(Collectors.toSet());

        show.setEvent(event);
        show.setRoom(room);
        show.setArtists(artists);
        show = showRepository.save(show);
        EntitySyncUtil.syncShowArtistRelationship(show);
        artistRepository.saveAll(artists);

        return show;
    }

    @Override
    public List<Show> findShowsByEventId(Long eventId) {
        LOGGER.debug("find Shows by EventId {}", eventId);
        var event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        return showRepository.findByEventOrderByDateAsc(event);
    }

    @Override
    public Show getShowWithRoomAndSectors(Long id) {
        LOGGER.debug("Find show with id {} including room and sectors", id);
        return showRepository
            .findByIdWithRoomAndSectors(id)
            .orElseThrow(() -> new EntityNotFoundException("Show not found"));
    }

    @Override
    public List<Show> findShowsBetween(LocalDateTime start, LocalDateTime end) {
        LOGGER.debug("Finding shows between {} and {}", start, end);
        return showRepository.findShowsBetween(start, end);
    }
}
