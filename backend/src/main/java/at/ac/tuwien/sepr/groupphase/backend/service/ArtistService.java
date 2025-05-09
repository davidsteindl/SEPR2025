package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;

import java.util.List;

public interface ArtistService {
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
}
