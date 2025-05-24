package at.ac.tuwien.sepr.groupphase.backend.repository.ticket;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT o.id FROM Order o
        WHERE o.userId = :userId
          AND o.orderType = :orderType
          AND ((:past = true AND EXISTS (
                SELECT t FROM o.tickets t WHERE t.show.date < :now))
            OR (:past = false AND EXISTS (
                SELECT t FROM o.tickets t WHERE t.show.date >= :now)))
        """)
    Page<Long> findOrderIdsByTypeAndPast(
        @Param("userId") Long userId,
        @Param("orderType") OrderType orderType,
        @Param("past") boolean past,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.tickets t
            JOIN FETCH t.show s
            JOIN FETCH s.room r
            JOIN FETCH r.eventLocation
            WHERE o.id IN :ids
        """)
    List<Order> findAllWithDetailsByIdIn(@Param("ids") List<Long> ids);

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.tickets t
            LEFT JOIN FETCH t.show s
            LEFT JOIN FETCH s.event e
            LEFT JOIN FETCH e.location l
            LEFT JOIN FETCH t.sector sec
            WHERE o.id = :id
        """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

}
