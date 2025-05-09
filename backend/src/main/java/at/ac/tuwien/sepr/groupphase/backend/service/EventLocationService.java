package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;

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
}
