package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;

import java.util.List;

public interface EventService {
    /**
     * Returns the event with the given ID.
     *
     * @param id the ID of the event to retrieve
     * @return the event with the given ID, or null if no such event exists
     */
    Event getEventById(Long id);

    /**
     * Returns all events.
     *
     * @return a list of all events
     */
    List<Event> getAllEvents();

    /**
     * Saves the given event.
     *
     * @param event the event to save
     * @return the saved event
     */
    Event createEvent(Event event);

    /**
     * Returns the location of the event with the given id.
     *
     * @param eventId the id of the event
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
