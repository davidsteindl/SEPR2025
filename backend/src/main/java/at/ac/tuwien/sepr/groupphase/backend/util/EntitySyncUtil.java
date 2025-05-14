package at.ac.tuwien.sepr.groupphase.backend.util;


import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;

import java.util.HashSet;
import java.util.Set;

public class EntitySyncUtil {

    /**
     * Synchronisiert die Beziehung von Show zu Artists und zurück.
     *
     * @param show die Show, deren Artists synchronisiert werden sollen
     */
    public static void syncShowArtistRelationship(Show show) {
        if (show.getArtists() == null) {
            return;
        }

        for (Artist artist : show.getArtists()) {
            if (artist.getShows() == null) {
                Set<Show> newShows = new HashSet<>();
                newShows.add(show);
                artist.setShows(newShows);
            } else {
                artist.getShows().add(show);
            }
        }
    }

    /**
     * Synchronisiert die Beziehung von Artist zu Shows und zurück.
     *
     * @param artist der Artist, dessen Shows synchronisiert werden sollen
     */
    public static void syncArtistShowRelationship(Artist artist) {
        if (artist.getShows() == null) {
            return;
        }

        for (Show show : artist.getShows()) {
            if (show.getArtists() == null) {
                Set<Artist> newArtists = new HashSet<>();
                newArtists.add(artist);
                show.setArtists(newArtists);
            } else {
                show.getArtists().add(artist);
            }
        }
    }
}