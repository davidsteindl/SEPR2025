package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    /**
     * Find an artist by their stage name.
     *
     * @param stagename the stage name of the artist
     * @return an Optional containing the artist if found, or empty if not found
     */
    Optional<Artist> findByStagename(String stagename);
}
