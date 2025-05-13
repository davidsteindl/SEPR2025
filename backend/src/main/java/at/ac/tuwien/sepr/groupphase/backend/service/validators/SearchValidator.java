package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class SearchValidator {

    public SearchValidator() {

    }

    /**
     * Validates the search criteria for artists.
     *
     * @param artistSearchDto the search criteria
     * @throws ValidationException if any validation fails
     */
    public void validateForArtists(ArtistSearchDto artistSearchDto) {
        if (artistSearchDto == null) {
            throw new ValidationException("Search criteria must not be null");
        }

        if (artistSearchDto.getPage() == null || artistSearchDto.getPage() < 0) {
            throw new ValidationException("Page index must be non-negative");
        }
        if (artistSearchDto.getSize() == null || artistSearchDto.getSize() <= 0) {
            throw new ValidationException("Page size must be greater than zero");
        }

        boolean hasFirstname = artistSearchDto.getFirstname() != null && !artistSearchDto.getFirstname().isBlank();
        boolean hasLastname = artistSearchDto.getLastname() != null && !artistSearchDto.getLastname().isBlank();
        boolean hasStagename = artistSearchDto.getStagename() != null && !artistSearchDto.getStagename().isBlank();

        if (!hasFirstname && !hasLastname && !hasStagename) {
            throw new ValidationException("At least one of the following fields must be filled: firstname, lastname, stagename.");
        }

        if (artistSearchDto.getFirstname() != null && artistSearchDto.getFirstname().length() > 100) {
            throw new ValidationException("Firstname filter must not exceed 100 characters");
        }

        if (artistSearchDto.getLastname() != null && artistSearchDto.getLastname().length() > 100) {
            throw new ValidationException("Lastname filter must not exceed 100 characters");
        }

        if (artistSearchDto.getStagename() != null && artistSearchDto.getStagename().length() > 100) {
            throw new ValidationException("Stagename filter must not exceed 100 characters");
        }
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
