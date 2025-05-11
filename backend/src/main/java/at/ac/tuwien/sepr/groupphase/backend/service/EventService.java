package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

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
    Event createEvent(Event event) throws ValidationException;
}
