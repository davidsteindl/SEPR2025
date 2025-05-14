package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.util.MinMaxTime;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class EventValidator {
    private final EventRepository eventRepository;
    private final ShowRepository showRepository;

    @Autowired
    public EventValidator(EventRepository eventRepository, ShowRepository showRepository) {
        this.eventRepository = eventRepository;
        this.showRepository = showRepository;
    }

    public void validateDuration(Long eventId) throws ValidationException {
        MinMaxTime result = showRepository.findMinStartAndMaxEndByEventId(eventId);
        if (result.getMinDate() == null || result.getMaxEnd() == null) {
            return;
        }

        long totalShowDurationMinutes = Duration.between(
            result.getMinDate().toLocalDateTime(),
            result.getMaxEnd().toLocalDateTime()
        ).toMinutes();

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found"));
        if (event.getDuration() < totalShowDurationMinutes) {
            throw new ValidationException("Event duration is less than the total show duration",
                List.of("Event duration is less than the total show duration"));
        }
    }
}
