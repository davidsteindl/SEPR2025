package at.ac.tuwien.sepr.groupphase.backend.repository.ticket;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.OrderGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderGroupRepository extends JpaRepository<OrderGroup, Long> {

    @Query("""
            SELECT DISTINCT og FROM OrderGroup og
            JOIN og.orders o
            JOIN o.tickets t
            JOIN t.show s
            WHERE og.userId = :userId
              AND o.orderType = :reservationType
              AND s.date >= CURRENT_TIMESTAMP
        """)
    Page<OrderGroup> findReservations(
        @Param("userId") Long userId,
        @Param("reservationType") OrderType reservationType,
        Pageable pageable
    );


    @Query("""
            SELECT DISTINCT og FROM OrderGroup og
            JOIN og.orders o
            JOIN o.tickets t
            JOIN t.show s
            WHERE og.userId = :userId
              AND o.orderType IN :validOrderTypes
              AND s.date < CURRENT_TIMESTAMP
        """)
    Page<OrderGroup> findPaidOrRefundedPast(
        @Param("userId") Long userId,
        @Param("validOrderTypes") List<OrderType> validOrderTypes,
        Pageable pageable
    );

    @Query("""
            SELECT DISTINCT og FROM OrderGroup og
            JOIN og.orders o
            JOIN o.tickets t
            JOIN t.show s
            WHERE og.userId = :userId
              AND o.orderType IN :validOrderTypes
              AND s.date >= CURRENT_TIMESTAMP
        """)
    Page<OrderGroup> findPaidOrRefundedUpcoming(
        @Param("userId") Long userId,
        @Param("validOrderTypes") List<OrderType> validOrderTypes,
        Pageable pageable
    );

    @Query("""
            SELECT og FROM OrderGroup og
            WHERE og.id = :groupId AND og.userId = :userId
        """)
    Optional<OrderGroup> findByIdWithDetails(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Query("""
        SELECT og FROM OrderGroup og
        LEFT JOIN FETCH og.orders o
        WHERE og.id = :id
        """)
    Optional<OrderGroup> findByIdWithOrders(@Param("id") Long id);
}
