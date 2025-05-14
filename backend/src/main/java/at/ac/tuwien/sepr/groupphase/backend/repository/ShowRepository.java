package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.util.MinMaxTime;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    @Query("SELECT DISTINCT s FROM Show s LEFT JOIN FETCH s.artists")
    List<Show> findAllWithArtists();

    @Query(value = """
        SELECT
        MIN(s.date) as min_date,
        MAX(s.date + s.duration * INTERVAL '1' MINUTE) as max_end
        FROM show s
        WHERE s.event_id = :eventId
        """, nativeQuery = true)
    MinMaxTime findMinStartAndMaxEndByEventId(@Param("eventId") Long eventId);

    @Query("SELECT s FROM Show s LEFT JOIN FETCH s.artists WHERE s.id = :id")
    Optional<Show> findByIdWithArtists(@Param("id") Long id);
}
