package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventValidator {

    private final EventRepository eventRepository;
    private final EventLocationRepository locationRepository;
    private final ShowRepository showRepository;

    @Autowired
    public EventValidator(EventRepository eventRepository,
                          EventLocationRepository locationRepository,
                          ShowRepository showRepository) {
        this.eventRepository = eventRepository;
        this.locationRepository = locationRepository;
        this.showRepository = showRepository;
    }

    /**
     * Validate event before creation.
     *
     * @param event the event to validate
     *
     * @throws ValidationException if any validation errors
     */
    public void validateForCreate(Event event) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (event.getName() == null || event.getName().isBlank()) {
            errors.add("Name must not be blank");
        } else if (event.getName().length() > 100) {
            errors.add("Name must not exceed 100 characters");
        }

        if (event.getDescription() == null || event.getDescription().isBlank()) {
            errors.add("Description must not be blank");
        } else if (event.getDescription().length() > 500) {
            errors.add("Description must not exceed 500 characters");
        }

        LocalDateTime start = event.getDateTime();
        if (start == null) {
            errors.add("Start date/time must not be null");
        }

        int duration = event.getDuration();
        if (duration < 10 || duration > 10000) {
            errors.add("Duration must be between 10 and 10000 minutes");
        }

        if (event.getLocation() == null || event.getLocation().getId() == null
            || !locationRepository.existsById(event.getLocation().getId())) {
            String locName = event.getLocation() != null && event.getLocation().getName() != null
                ? "'" + event.getLocation().getName() + "'"
                : "the specified location";
            errors.add("Location not found for " + locName);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed for event creation", errors);
        }
    }

    /**
     * Validate event before update, including existing show's timeframe.
     *
     * @param id the ID of the event to update
     * @param event the new event data
     * @throws ValidationException if any validation errors
     */
    public void validateForUpdate(Long id, Event event) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (event.getName() == null || event.getName().isBlank()) {
            errors.add("Name must not be blank");
        } else if (event.getName().length() > 100) {
            errors.add("Name must not exceed 100 characters");
        }

        if (event.getDescription() == null || event.getDescription().isBlank()) {
            errors.add("Description must not be blank");
        } else if (event.getDescription().length() > 500) {
            errors.add("Description must not exceed 500 characters");
        }

        if (event.getDateTime() == null) {
            errors.add("Start date/time must not be null");
        }

        int duration = event.getDuration();
        if (duration < 10 || duration > 10000) {
            errors.add("Duration must be between 10 and 10000 minutes");
        }

        if (event.getLocation() == null || event.getLocation().getId() == null
            || !locationRepository.existsById(event.getLocation().getId())) {
            String locName = event.getLocation() != null && event.getLocation().getName() != null
                ? "'" + event.getLocation().getName() + "'"
                : "the specified location";
            errors.add("Location not found for " + locName);
        }

        Event existing = eventRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Event not found"));

        LocalDateTime newStart = event.getDateTime();
        LocalDateTime newEnd = newStart.plusMinutes(event.getDuration());
        List<Show> shows = showRepository.findByEventOrderByDateAsc(existing);

        for (Show s : shows) {
            LocalDateTime start = s.getDate().truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime end = start.plusMinutes(s.getDuration());
            if (start.isBefore(newStart) || end.isAfter(newEnd)) {
                errors.add("Show with name " + s.getName() + " outside event timeframe");
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed for event update", errors);
        }
    }

}
