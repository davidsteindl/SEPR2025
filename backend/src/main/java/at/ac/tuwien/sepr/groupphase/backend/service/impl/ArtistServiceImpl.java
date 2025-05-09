package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ArtistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ArtistServiceImpl implements ArtistService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ArtistRepository artistRepository;
    private final ShowRepository showRepository;

    @Autowired
    public ArtistServiceImpl(ArtistRepository artistRepository, ShowRepository showRepository) {
        this.artistRepository = artistRepository;
        this.showRepository = showRepository;
    }

    @Override
    public Artist getArtistById(Long id) {
        LOGGER.info("Find artist with id {}", id);
        return artistRepository.findById(id).orElse(null);
    }

    @Override
    public List<Artist> getAllArtists() {
        LOGGER.info("Get all artists");
        return artistRepository.findAll();
    }

    @Override
    public Artist createArtist(Artist artist) {
        LOGGER.info("Save artist {}", artist);
        Set<Show> existingShows = new HashSet<>();
        if (artist.getShows() != null) {
            for (Show show : artist.getShows()) {
                Show existingShow = showRepository.findById(show.getId())
                    .orElseThrow(() -> new ValidationException("Show with id " + show.getId() + " not found"));
                existingShows.add(existingShow);
            }
        }
        artist.setShows(existingShows);
        return artistRepository.save(artist);
    }
}
