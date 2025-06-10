package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ArtistService;
import at.ac.tuwien.sepr.groupphase.backend.util.EntitySyncUtil;
import jakarta.transaction.Transactional;
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
        LOGGER.debug("Find artist with id {}", id);
        return artistRepository.findByIdWithShows(id).orElse(null);
    }

    @Override
    public List<Artist> getAllArtists() {
        LOGGER.debug("Get all artists");
        return artistRepository.findAllWithShows();
    }

    @Override
    @Transactional
    public Artist createArtist(Artist artist) throws ValidationException {
        LOGGER.debug("Saving artist with name '{}'", artist.getStagename());

        Set<Show> existingShows = new HashSet<>();
        if (artist.getShows() != null) {
            for (Show show : artist.getShows()) {
                Show existingShow = showRepository.findByIdWithArtists(show.getId())
                    .orElseThrow(() -> new ValidationException("Show not found", List.of("Show not found")));
                existingShows.add(existingShow);
            }
        }

        artist.setShows(existingShows);
        artist = artistRepository.save(artist);
        EntitySyncUtil.syncArtistShowRelationship(artist);
        showRepository.saveAll(existingShows);

        return artist;
    }
}
