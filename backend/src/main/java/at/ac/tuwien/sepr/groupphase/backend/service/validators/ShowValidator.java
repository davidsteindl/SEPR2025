package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.util.MinMaxTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ShowValidator {

    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;
    private final RoomRepository roomRepository;
    private final ShowRepository showRepository;

    @Autowired
    public ShowValidator(EventRepository eventRepository, ArtistRepository artistRepository, ShowRepository showRepository, RoomRepository roomRepository) {
        this.eventRepository = eventRepository;
        this.artistRepository = artistRepository;
        this.showRepository = showRepository;
        this.roomRepository = roomRepository;
    }

    public void validateForCreate(Show show) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (show.getEvent() == null || show.getEvent().getId() == null) {
            errors.add("Event ID is null");
        } else if (!eventRepository.existsById(show.getEvent().getId())) {
            errors.add("Event with ID " + show.getEvent().getId() + " not found");
        } else if (!validateDuration(show.getEvent().getId(), show.getDate(), show.getDuration())) {
            errors.add("Event duration is less than the total duration of all shows");
        }

        if (show.getRoom() == null || show.getRoom().getId() == null) {
            errors.add("Room ID is null");
        } else if (!roomRepository.existsById(show.getRoom().getId())) {
            errors.add("Room with ID " + show.getRoom().getId() + " not found");
        }

        if (show.getArtists() == null || show.getArtists().isEmpty()) {
            errors.add("Artist list is empty or null");
        } else {
            for (Artist a : show.getArtists()) {
                if (a.getId() == null) {
                    errors.add("Artist ID is null");
                } else if (!artistRepository.existsById(a.getId())) {
                    errors.add("Artist with ID " + a.getId() + " not found");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed for show creation", errors);
        }
    }

    public boolean validateDuration(Long eventId, LocalDateTime showStart, int showDuration) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found"));

        MinMaxTime result = showRepository.findMinStartAndMaxEndByEventId(eventId);
        LocalDateTime min = result.getMinDate() != null ? result.getMinDate().toLocalDateTime() : null;
        LocalDateTime max = result.getMaxEnd() != null ? result.getMaxEnd().toLocalDateTime() : null;

        LocalDateTime newEnd = showStart.plusMinutes(showDuration);

        if (min == null || showStart.isBefore(min)) {
            min = showStart;
        }
        if (max == null || newEnd.isAfter(max)) {
            max = newEnd;
        }

        long totalBlockMinutes = Duration.between(min, max).toMinutes();
        return event.getDuration() >= totalBlockMinutes;
    }
}
