package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordOtt;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.time.LocalDateTime;

@Repository
public interface OtTokenRepository extends JpaRepository<PasswordOtt, Long> {

    @Query("SELECT s.userId FROM PasswordOtt s WHERE s.otToken = :otToken AND s.consumed = false AND s.validUntil > CURRENT_TIMESTAMP")
    Long findUserIdByOtTokenIfValid(@Param("otToken") String otToken);


    @Modifying
    @Transactional
    @Query("UPDATE PasswordOtt p SET p.consumed = true WHERE p.otToken = :otToken")
    void markConsumed(@Param("otToken") String otToken);



    Optional<PasswordOtt> findByOtTokenAndConsumedFalseAndValidUntilAfter(String token, LocalDateTime now);

}
