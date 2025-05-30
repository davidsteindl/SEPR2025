package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordOtt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OttPasswordRepository extends JpaRepository<PasswordOtt, Long> {
}
