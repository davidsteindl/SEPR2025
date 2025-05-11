package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import jakarta.validation.ValidationException;

public class SearchValidator {

    public SearchValidator() {
    }

    /**
     * Validates the search criteria for events.
     *
     * @param eventSearchDto the search criteria
     * @throws ValidationException if any validation fails
     */
    public void validateForEvents(EventSearchDto eventSearchDto) {
        if (eventSearchDto == null) {
            throw new ValidationException("Search criteria must not be null");
        }

        if (eventSearchDto.getPage() == null || eventSearchDto.getPage() < 0) {
            throw new ValidationException("Page index must be non-negative");
        }
        if (eventSearchDto.getSize() == null || eventSearchDto.getSize() <= 0) {
            throw new ValidationException("Page size must be greater than zero");
        }

        if (eventSearchDto.getName() != null && eventSearchDto.getName().length() > 100) {
            throw new ValidationException("Event name filter must not exceed 100 characters");
        }

        if (eventSearchDto.getType() != null && !eventSearchDto.getType().isBlank()) {
            boolean valid = false;
            for (var cat : Event.EventCategory.values()) {
                if (cat.name().equalsIgnoreCase(eventSearchDto.getType())
                    || cat.getDisplayName().equalsIgnoreCase(eventSearchDto.getType())) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                throw new ValidationException("Invalid event type: " + eventSearchDto.getType());
            }
        }

        if (eventSearchDto.getDescription() != null && eventSearchDto.getDescription().length() > 500) {
            throw new ValidationException("Description filter must not exceed 500 characters");
        }

        if (eventSearchDto.getDuration() != null && eventSearchDto.getDuration() < 0) {
            throw new ValidationException("Duration filter must be non-negative");
        }
    }
}
