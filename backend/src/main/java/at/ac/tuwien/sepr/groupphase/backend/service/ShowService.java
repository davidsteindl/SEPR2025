package at.ac.tuwien.sepr.groupphase.backend.service;


import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShowService {
    /**
     * Returns the show with the given ID.
     *
     * @param id the ID of the show to retrieve
     * @return the show with the given ID, or null if no such show exists
     */
    Show getShowById(Long id);

    /**
     * Returns all shows.
     *
     * @return a list of all shows
     */
    List<Show> getAllShows();

    /**
     * Saves the given show.
     *
     * @param show the show to save
     * @return the saved show
     */
    Show createShow(Show show) throws ValidationException;


    /**
     * Returns all shows for the given event ID.
     *
     * @param eventId the ID of the event to retrieve shows for
     * @return a list of shows for the given event ID
     */
    public List<Show> findShowsByEventId(Long eventId);

    /**
     * Retrieves a paginated list of shows for a specific event.
     *
     * @param eventId the ID of the event whose shows should be fetched
     * @param pageable pagination and sorting information
     * @return a {@link Page} of {@link Show} entities linked to the given event
     */
    Page<Show> getPagedShowsForEvent(Long eventId, Pageable pageable);
}

