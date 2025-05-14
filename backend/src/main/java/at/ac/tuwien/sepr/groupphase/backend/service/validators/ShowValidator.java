package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ShowValidator {

    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;

    @Autowired
    public ShowValidator(EventRepository eventRepository, ArtistRepository artistRepository) {
        this.eventRepository = eventRepository;
        this.artistRepository = artistRepository;
    }

    public void validateForCreate(Show show) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (show.getEvent() == null || show.getEvent().getId() == null) {
            errors.add("Event ID is null");
        } else if (!eventRepository.existsById(show.getEvent().getId())) {
            errors.add("Event with ID " + show.getEvent().getId() + " not found");
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
}
