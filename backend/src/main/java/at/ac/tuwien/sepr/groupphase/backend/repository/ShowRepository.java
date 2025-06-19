package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.util.MinMaxTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long>, JpaSpecificationExecutor<Show> {

    @Query("SELECT DISTINCT s FROM Show s LEFT JOIN FETCH s.artists")
    List<Show> findAllWithArtists();

    @Query(value = """
        SELECT
            MIN(s.date) AS min_date,
            MAX(DATEADD('MINUTE', s.duration, s.date)) AS max_end
        FROM show s
        WHERE s.event_id = :eventId
        """, nativeQuery = true)
    MinMaxTime findMinStartAndMaxEndByEventId(@Param("eventId") Long eventId);


    @Query("SELECT s FROM Show s LEFT JOIN FETCH s.artists WHERE s.id = :id")
    Optional<Show> findByIdWithArtists(@Param("id") Long id);

    @Query("""
            SELECT s FROM Show s
            LEFT JOIN FETCH s.artists
            LEFT JOIN FETCH s.room r
            LEFT JOIN FETCH r.seats
            LEFT JOIN FETCH r.sectors
            LEFT JOIN FETCH s.event
            WHERE s.id = :id
        """)
    Optional<Show> findDetailedById(@Param("id") Long id);

    List<Show> findByEventOrderByDateAsc(Event event);

    @EntityGraph(attributePaths = {"artists"})
    Page<Show> findByEvent(Event event, Pageable pageable);

    @Query("SELECT DISTINCT s.event FROM Show s JOIN s.artists a WHERE a.id = :artistId")
    Page<Event> findEventsByArtistId(@Param("artistId") Long artistId, Pageable pageable);

    @EntityGraph(attributePaths = {"artists"})
    Page<Show> findAllByEvent_Location_IdOrderByDateAsc(Long locationId, Pageable pageable);


    @EntityGraph(attributePaths = {"room.seats", "room.sectors"})
    @Query("SELECT s FROM Show s WHERE s.id = :id")
    Optional<Show> findByIdWithRoomAndSectors(@Param("id") Long id);

    @EntityGraph(attributePaths = {"room.sectors", "event"})
    Page<Show> findAll(Specification<Show> spec, Pageable pageable);

    @Query("""
            SELECT s FROM Show s
            WHERE s.date BETWEEN :start AND :end
        """)
    List<Show> findShowsBetween(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
}
