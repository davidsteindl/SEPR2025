package at.ac.tuwien.sepr.groupphase.backend.repository.ticket;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByShowId(Long showId);

    long countByStatus(TicketStatus status);

    @Query("""
        SELECT t.show.event, COUNT(t.id) as ticketCount
        FROM Ticket t
        WHERE t.status = 'BOUGHT' AND t.show.event.dateTime BETWEEN CURRENT_TIMESTAMP AND :endDate
        GROUP BY t.show.event
        ORDER BY ticketCount DESC, t.show.event.dateTime ASC
        """)
    List<Object[]> findTopTenEventsOrderByTicketCountDesc(@Param("endDate") LocalDateTime endDate, Pageable pageable);


    @Query("""
        SELECT t.show.event, COUNT(t.id) as ticketCount
        FROM Ticket t
        WHERE t.status = 'BOUGHT'
                AND t.show.event.category = :category
                AND t.show.event.dateTime BETWEEN CURRENT_TIMESTAMP AND :endDate
        GROUP BY t.show.event
        ORDER BY ticketCount DESC, t.show.event.dateTime ASC
        """)
    List<Object[]> findTopTenEventsByCategoryOrderByTicketCountDesc(@Param("category") Event.EventCategory category, @Param("endDate") LocalDateTime endDate,
                                                                    Pageable pageable);

    List<Ticket> findByShowAndStatus(Show show, TicketStatus status);
}
