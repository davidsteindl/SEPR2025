package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Hold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoldRepository extends JpaRepository<Hold, Long> {

    List<Hold> findByShowId(Long showId);
}
