package at.ac.tuwien.sepr.groupphase.backend.service;


import java.util.List;


/**
 * Service interface for searching and filtering artists, locations, events, and performances.
 */
public interface SearchService {

    /**
     * Search artists by first name, last name, or band names or pseudonyms.
     * @param criteria the search criteria
     * @return list of matching artists
     */
    List<ArtistDto> searchArtists(ArtistSearchDto criteria);

    /**
     * Search locations by name, street, city, country, or postal code.
     * @param criteria the search criteria
     * @return list of matching locations
     */
    List<LocationDto> searchLocations(LocationSearchDto criteria);

    /**
     * Search events by title, type, duration (± 30min), or content.
     * @param criteria the search criteria
     * @return list of matching events
     */
    List<EventDto> searchEvents(EventSearchDto criteria);

    /**
     * Search performances by date/time, price (± tolerance), event, or hall.
     * @param criteria the search criteria
     * @return list of matching performances
     */
    List<PerformanceDto> searchPerformances(PerformanceSearchDto criteria);
}
