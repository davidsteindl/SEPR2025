package at.ac.tuwien.sepr.groupphase.backend.repository.ticket;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;


public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN FETCH o.tickets t
            JOIN FETCH t.show s
            WHERE o.userId = :userId
              AND o.orderType = :orderType
              AND (:past = true AND s.date < :now OR :past = false AND s.date >= :now)
        """)
    Page<Order> findOrdersByTypeAndPast(
        @Param("userId") Long userId,
        @Param("orderType") OrderType orderType,
        @Param("past") boolean past,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

}
