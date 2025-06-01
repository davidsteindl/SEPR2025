package at.ac.tuwien.sepr.groupphase.backend.repository.ticket;

import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.OrderGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderGroupRepository extends JpaRepository<OrderGroup, Long> {

    @Query("""
            SELECT DISTINCT og FROM OrderGroup og
            JOIN og.orders o
            JOIN o.tickets t
            JOIN t.show s
            WHERE og.userId = :userId
            AND o.orderType != at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType.CANCELLATION
            AND (
                (:isReservation = TRUE AND o.orderType = at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType.RESERVATION)
                OR
                (:isReservation = FALSE AND o.orderType IN (
                    at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType.ORDER,
                    at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType.REFUND
                )
                AND (
                    (:past = TRUE AND s.date < CURRENT_TIMESTAMP)
                    OR (:past = FALSE AND s.date >= CURRENT_TIMESTAMP)
                ))
            )
        """)
    Page<OrderGroup> findByCategory(
        @Param("userId") Long userId,
        @Param("isReservation") boolean isReservation,
        @Param("past") boolean past,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT og FROM OrderGroup og
            LEFT JOIN FETCH og.orders o
            LEFT JOIN FETCH o.tickets t
            LEFT JOIN FETCH t.show s
            WHERE og.id = :id
        """)
    Optional<OrderGroup> findByIdWithOrdersAndTickets(@Param("id") Long id);
}
