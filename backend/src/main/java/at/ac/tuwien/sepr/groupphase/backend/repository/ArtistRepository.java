package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    /**
     * Finds all artists where the given text occurs in the firstname, lastname, or stagename (case-insensitive).
     *
     * @return a list of matching artists
     */
    List<Artist> findByFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCaseOrStagenameContainingIgnoreCase(
        String firstname, String lastname, String stagename);

}
