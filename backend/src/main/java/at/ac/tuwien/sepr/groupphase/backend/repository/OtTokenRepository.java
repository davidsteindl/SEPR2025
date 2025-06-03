package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordOtt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.time.LocalDateTime;

@Repository
public interface OtTokenRepository extends JpaRepository<PasswordOtt, Long> {

    @Query("SELECT s.userId FROM PasswordOtt s WHERE s.otToken = :otToken")
    Long findUserIdByOtToken(@Param("otToken") String otToken);


    @Modifying
    @Query("UPDATE PasswordOtt p SET p.consumed = true WHERE p.id = :id")
    void markConsumed(@Param("id") Long id);

    Optional<PasswordOtt> findByOtTokenAndConsumedFalseAndValidUntilAfter(String token, LocalDateTime now);

}
