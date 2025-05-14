package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    @Query("SELECT DISTINCT a FROM Artist a LEFT JOIN FETCH a.shows")
    List<Artist> findAllWithShows();

    @Query("SELECT a FROM Artist a LEFT JOIN FETCH a.shows WHERE a.id = :id")
    Optional<Artist> findByIdWithShows(@Param("id") Long id);
}
