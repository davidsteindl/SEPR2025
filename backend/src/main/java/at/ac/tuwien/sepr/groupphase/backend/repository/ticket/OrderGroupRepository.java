package at.ac.tuwien.sepr.groupphase.backend.repository.ticket;

import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.OrderGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderGroupRepository extends JpaRepository<OrderGroup, Long> {
}
