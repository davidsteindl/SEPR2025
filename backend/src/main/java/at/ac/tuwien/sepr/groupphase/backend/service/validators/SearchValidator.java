package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
     * Validates the search criteria for event locations.
     *
     * @param eventLocationSearchDto the search criteria
     * @throws ValidationException if any validation fails
     */
    public void validateForEventLocations(EventLocationSearchDto eventLocationSearchDto) throws ValidationException {
        List<String> validationErrors = new ArrayList<>();

        if (eventLocationSearchDto == null) {
            validationErrors.add("Eventlocation search request must not be null");
            throw new ValidationException("Validation of eventlocation search request failed", validationErrors);
        }

        if (eventLocationSearchDto.getPage() == null || eventLocationSearchDto.getPage() < 0) {
            validationErrors.add("Page index must be non- negative");
        }

        if (eventLocationSearchDto.getSize() == null || eventLocationSearchDto.getSize() <= 0) {
            validationErrors.add("Page size must be greater than zero");
        }

        boolean hasName = eventLocationSearchDto.getName() != null && !eventLocationSearchDto.getName().isBlank();
        boolean hasStreet = eventLocationSearchDto.getStreet() != null && !eventLocationSearchDto.getStreet().isBlank();
        boolean hasCity = eventLocationSearchDto.getCity() != null && !eventLocationSearchDto.getCity().isBlank();
        boolean hasCountry = eventLocationSearchDto.getCountry() != null && !eventLocationSearchDto.getCountry().isBlank();
        boolean hasPostalCode = eventLocationSearchDto.getPostalCode() != null && !eventLocationSearchDto.getPostalCode().isBlank();

        if (!hasName && !hasStreet && !hasCity && !hasCountry && !hasPostalCode) {
            validationErrors.add(
                "At least one of the following fields must be filled: eventlocation name, eventlocation street, eventlocation city, eventlocation country, eventlocation postalcode .");
        }

        if (eventLocationSearchDto.getName() != null && eventLocationSearchDto.getName().length() > 100) {
            validationErrors.add("Eventlocation name filter must not exceed 100 characters");
        }

        if (eventLocationSearchDto.getStreet() != null && eventLocationSearchDto.getStreet().length() > 100) {
            validationErrors.add("Eventlocation street filter must not exceed 100 characters");
        }

        if (eventLocationSearchDto.getCity() != null && eventLocationSearchDto.getCity().length() > 100) {
            validationErrors.add("Eventlocation city filter must not exceed 100 characters");
        }

        if (eventLocationSearchDto.getPostalCode() != null && eventLocationSearchDto.getPostalCode().length() > 100) {
            validationErrors.add("Eventlocation postalcode filter must not exceed 100 characters");
        }

        if (!(validationErrors.isEmpty())) {
            throw new ValidationException("Validation of eventlocation search request failed", validationErrors);
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
            validationErrors.add("At least one of the following fields must be filled: eventname, event category, event description, event duration .");
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

    /**
     * Validates the search criteria for time.
     *
     * @throws ValidationException if any validation fails
     */
    public void validateForShows(ShowSearchDto criteria) throws ValidationException {
        List<String> validationErrors = new ArrayList<>();

        if (criteria == null) {
            validationErrors.add("Show search request must not be null");
            throw new ValidationException("Validation of show search request failed", validationErrors);
        }

        if (criteria.getPage() == null || criteria.getPage() < 0) {
            validationErrors.add("Page index must be non-negative");
        }

        if (criteria.getSize() == null || criteria.getSize() <= 0) {
            validationErrors.add("Page size must be greater than zero");
        }

        boolean hasName = criteria.getName() != null && !criteria.getName().isBlank();
        boolean hasEventName = criteria.getEventName() != null && !criteria.getEventName().isBlank();
        boolean hasRoomName = criteria.getRoomName() != null && !criteria.getRoomName().isBlank();
        boolean hasstartDate = criteria.getStartDate() != null;
        boolean hasEndDate = criteria.getEndDate() != null;
        boolean hasMinPrice = criteria.getMinPrice() != null;
        boolean hasMaxPrice = criteria.getMaxPrice() != null;

        if (!hasName && !hasEventName && !hasRoomName && !hasstartDate && !hasEndDate && !hasMinPrice && !hasMaxPrice) {
            validationErrors.add(
                "At least one of the following fields must be filled: name, event name, room name, start date, end date, minimum price, maximum price.");
        }

        boolean hasName = criteria.getName() != null && !criteria.getName().isBlank();
        boolean hasEventName = criteria.getEventName() != null && !criteria.getEventName().isBlank();
        boolean hasRoomName = criteria.getRoomName() != null && !criteria.getRoomName().isBlank();
        boolean hasstartDate = criteria.getStartDate() != null;
        boolean hasEndDate = criteria.getEndDate() != null;
        boolean hasMinPrice = criteria.getMinPrice() != null;
        boolean hasMaxPrice = criteria.getMaxPrice() != null;

        if (!hasName && !hasEventName && !hasRoomName && !hasstartDate && !hasEndDate && !hasMinPrice && !hasMaxPrice) {
            validationErrors.add(
                "At least one of the following fields must be filled: name, event name, room name, start date, end date, minimum price, maximum price.");
        }

        LocalDateTime start = criteria.getStartDate();
        LocalDateTime end = criteria.getEndDate();
        if (start != null && end != null && end.isBefore(start)) {
            validationErrors.add("End date must not be before start date");
        }

        if (criteria.getName() != null && criteria.getName().length() > 100) {
            validationErrors.add("Name filter must not exceed 100 characters");
        }

        if (criteria.getEventName() != null && criteria.getEventName().length() > 100) {
            validationErrors.add("Event name filter must not exceed 100 characters");
        }

        if (criteria.getRoomName() != null && criteria.getRoomName().length() > 100) {
            validationErrors.add("Room name filter must not exceed 100 characters");
        }

        BigDecimal minPrice = criteria.getMinPrice();
        BigDecimal maxPrice = criteria.getMaxPrice();
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            validationErrors.add("Minimum price must not be negative");
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            validationErrors.add("Maximum price must not be negative");
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            validationErrors.add("Minimum price must not be greater than maximum price");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of show search request failed", validationErrors);
        }
    }
}
