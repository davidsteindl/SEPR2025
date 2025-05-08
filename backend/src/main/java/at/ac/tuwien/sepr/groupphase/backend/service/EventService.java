package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;

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
     * Returns the artist with the given ID.
     *
     * @param id the ID of the artist to retrieve
     * @return the artist with the given ID, or null if no such artist exists
     */
    Artist getArtistById(Long id);

    /**
     * Returns all artists.
     *
     * @return a list of all artists
     */
    List<Artist> getAllArtists();

    /**
     * Saves the given artist.
     *
     * @param artist the artist to save
     * @return the saved artist
     */
    Artist createArtist(Artist artist);

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
    Show createShow(Show show);
}
