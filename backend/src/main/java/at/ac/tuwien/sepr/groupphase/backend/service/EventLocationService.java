package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventLocationService {
    /**
     * Returns the location of the event with the given id.
     *
     * @param eventId the id of the event to retrieve
     * @return the event location, or null if no such event location exists
     */
    EventLocation getEventLocationById(Long eventId);

    /**
     * Returns all event locations.
     *
     * @return a list of all event locations
     */
    List<EventLocation> getAllEventLocations();

    /**
     * Saves the given event location.
     *
     * @param eventLocation the event location to save
     * @return the saved event location
     */
    EventLocation createEventLocation(EventLocation eventLocation);

    /**
     * Returns a paginated list of all shows for the given event location.
     *
     * @param eventLocationId the id of the event location
     * @return a paginated list of shows for the event location
     */
    Page<ShowDetailDto> getShowsForEventLocation(Long eventLocationId, Pageable pageable);
}
