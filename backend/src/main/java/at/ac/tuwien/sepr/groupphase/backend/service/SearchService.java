package at.ac.tuwien.sepr.groupphase.backend.service;



import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.performance.PerformanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.performance.PerformanceSearchDto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.data.domain.Page;

import java.util.List;


/**
 * Service interface for searching and filtering artists, locations, events, and performances.
 */
public interface SearchService {

    /**
     * Search artists by first name, last name, or band names or pseudonyms.
     *
     * @param criteria the search criteria
     * @return page of matching artists
     */
    Page<ArtistSearchResultDto> searchArtists(ArtistSearchDto criteria) throws ValidationException;

    /**
     * Search eventlocations by name, street, city, country, or postal code.
     *
     * @param criteria the search criteria
     * @return page of matching eventlocations
     */
    Page<EventLocationDetailDto> searchEventLocations(EventLocationSearchDto criteria) throws ValidationException;

    /**
     * Search events by title, category, duration (± 30min), or content.
     *
     * @param criteria the search criteria
     * @return page of matching events
     */
    Page<EventSearchResultDto> searchEvents(EventSearchDto criteria) throws ValidationException;

    /**
     * Searches for shows based on the given criteria and returns a paginated list of results.
     *
     * @param criteria the search criteria including optional filters like date range, event, room, name, and price
     * @return a page of shows matching the given criteria
     * @throws ValidationException if the search criteria are invalid
     */
    Page<ShowSearchResultDto> searchShows(ShowSearchDto criteria) throws ValidationException;

}
