package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    public void validateForArtists(ArtistSearchDto artistSearchDto) throws ValidationException {
        List<String> validationErrors = new ArrayList<>();

        if (artistSearchDto == null) {
            throw new ValidationException("Validation of artist search request failed", validationErrors);
        }

        if (artistSearchDto.getPage() == null || artistSearchDto.getPage() < 0) {
            validationErrors.add("Page index must be non-negative");
        }
        if (artistSearchDto.getSize() == null || artistSearchDto.getSize() <= 0) {
            validationErrors.add("Page size must be greater than zero");
        }


        boolean hasFirstname = artistSearchDto.getFirstname() != null && !artistSearchDto.getFirstname().isBlank();
        boolean hasLastname = artistSearchDto.getLastname() != null && !artistSearchDto.getLastname().isBlank();
        boolean hasStagename = artistSearchDto.getStagename() != null && !artistSearchDto.getStagename().isBlank();

        if (!hasFirstname && !hasLastname && !hasStagename) {
            validationErrors.add("At least one of the following fields must be filled: firstname, lastname, stagename.");
        }

        if (artistSearchDto.getFirstname() != null && artistSearchDto.getFirstname().length() > 100) {
            validationErrors.add("Firstname filter must not exceed 100 characters");
        }

        if (artistSearchDto.getLastname() != null && artistSearchDto.getLastname().length() > 100) {
            validationErrors.add("Lastname filter must not exceed 100 characters");
        }

        if (artistSearchDto.getStagename() != null && artistSearchDto.getStagename().length() > 100) {
            validationErrors.add("Stagename filter must not exceed 100 characters");
        }

        if (!(validationErrors.isEmpty())) {
            throw new ValidationException("Validation of artist search request failed", validationErrors);
        }
    }

    /**
     * Validates the search criteria for events.
     *
     * @param eventSearchDto the search criteria
     * @throws ValidationException if any validation fails
     */
    public void validateForEvents(EventSearchDto eventSearchDto) throws ValidationException {
        List<String> validationErrors = new ArrayList<>();


        if (eventSearchDto == null) {
            validationErrors.add("Event search request must not be null");
            throw new ValidationException("Validation of event search request failed", validationErrors);
        }

        if (eventSearchDto.getPage() == null || eventSearchDto.getPage() < 0) {
            validationErrors.add("Page index must be non-negative");
        }
        if (eventSearchDto.getSize() == null || eventSearchDto.getSize() <= 0) {
            validationErrors.add("Page size must be greater than zero");
        }
        boolean hasName = eventSearchDto.getName() != null && !eventSearchDto.getName().isBlank();
        boolean hasCategory = eventSearchDto.getCategory() != null && !eventSearchDto.getCategory().isBlank();
        boolean hasDescription = eventSearchDto.getDescription() != null && !eventSearchDto.getDescription().isBlank();
        boolean hasDuration = eventSearchDto.getDuration() != null;

        if (!hasName && !hasCategory && !hasDescription && !hasDuration) {
            validationErrors.add("At least one of the following fields must be filled: eventname, eventcategory, eventdescription, eventduration .");
        }
        if (eventSearchDto.getName() != null && eventSearchDto.getName().length() > 100) {
            validationErrors.add("Event name filter must not exceed 100 characters");
        }

        if (eventSearchDto.getCategory() != null && !eventSearchDto.getCategory().isBlank()) {
            boolean valid = false;
            for (var cat : Event.EventCategory.values()) {
                if (cat.name().equalsIgnoreCase(eventSearchDto.getCategory())
                    || cat.getDisplayName().equalsIgnoreCase(eventSearchDto.getCategory())) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                validationErrors.add("Invalid event type: " + eventSearchDto.getCategory());
            }
        }

        if (eventSearchDto.getDescription() != null && eventSearchDto.getDescription().length() > 500) {
            validationErrors.add("Description filter must not exceed 500 characters");
        }

        if (eventSearchDto.getDuration() != null && eventSearchDto.getDuration() < 0) {
            validationErrors.add("Duration filter must be non-negative");
        }

        if (eventSearchDto.getDuration() != null && eventSearchDto.getDuration() > 10000) {
            validationErrors.add("Duration filter must not exceed 10000 minutes");
        }

        if (!(validationErrors.isEmpty())) {
            throw new ValidationException("Validation of event search request failed", validationErrors);
        }
    }
}
