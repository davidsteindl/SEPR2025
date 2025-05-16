package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventWithShowsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
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
     * Retrieves a paginated list of events associated with the given artist.
     *
     * @param artistId the ID of the artist whose events should be retrieved
     * @param pageable the pagination and sorting information
     * @return a page of {@link EventDetailDto} instances for the given artist
     */
    Page<EventDetailDto> getEventsByArtist(Long artistId, Pageable pageable);

    /**
     * Retrieves a detailed view of an event including all associated shows.
     *
     * @param eventId the ID of the event whose details and shows should be retrieved
     * @return an {@link EventWithShowsDto} containing the event details and its scheduled shows
     */
    EventWithShowsDto getEventWithShows(Long eventId);

    Page<ShowDetailDto> getPaginatedShowsForEvent(Long eventId, Pageable pageable);
}
