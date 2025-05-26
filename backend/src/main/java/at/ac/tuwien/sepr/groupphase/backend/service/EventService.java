package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventCategoryDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventTopTenDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * Updates the given event.
     *
     * @param event the event to update
     * @return the updated event
     */
    Event updateEvent(Long id, Event event) throws ValidationException;

    /**
     * Retrieves a paginated list of events associated with the given artist.
     *
     * @param artistId the ID of the artist whose events should be retrieved
     * @param pageable the pagination and sorting information
     * @return a page of {@link EventDetailDto} instances for the given artist
     */
    Page<EventDetailDto> getEventsByArtist(Long artistId, Pageable pageable);

    /**
     * Retrieves a paginated list of shows for a specific event.
     *
     * @param eventId  the ID of the event whose shows should be fetched
     * @param pageable pagination and sorting information
     * @return a {@link Page} of {@link Show} entities linked to the given event
     */
    Page<ShowDetailDto> getPaginatedShowsForEvent(Long eventId, Pageable pageable);

    /**
     * Retrieves a list of the top 10 events by category based on ticket sales.
     *
     * @param category the category of events to filter by
     * @return a list of the top 10 events in the specified category
     */
    List<EventTopTenDto> getTopTenEventsByCategory(String category) throws ValidationException;

    /**
     * Retrieves all event categories.
     *
     * @return a list of all event categories
     */
    List<EventCategoryDto> getAllEventCategories();
}
