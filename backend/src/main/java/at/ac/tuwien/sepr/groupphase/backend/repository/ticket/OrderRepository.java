package at.ac.tuwien.sepr.groupphase.backend.repository.ticket;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdAndOrderType(Long userId, OrderType orderType, Pageable pageable);

    @Query("""
            SELECT o FROM Order o
            JOIN o.tickets t
            WHERE o.userId = :userId
            AND o.orderType = at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType.ORDER
            AND t.show.date < CURRENT_TIMESTAMP
        """)
    Page<Order> findPastOrders(@Param("userId") Long userId, Pageable pageable);
}
