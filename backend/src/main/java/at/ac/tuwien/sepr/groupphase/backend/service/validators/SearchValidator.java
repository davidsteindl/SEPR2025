package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchDto;
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
    public void validateForEvents(EventSearchDto eventSearchDto) throws ValidationException {

        if (eventSearchDto.getPage() == null || eventSearchDto.getPage() < 0) {
            throw new IllegalArgumentException("Page index must be non-negative");
        }

        if (eventSearchDto.getSize() == null || eventSearchDto.getSize() <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        if (eventSearchDto.getDuration() != null && eventSearchDto.getDuration() < 0) {
            throw new IllegalArgumentException("Duration must be non-negative");
        }
    }
}
