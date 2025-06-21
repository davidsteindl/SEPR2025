package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.LockedUserDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, Long> {

    /**
     * Finds an application user in the persistent datastore based on their email.
     *
     * @param email the email of the user
     * @return the user associated with the provided email
     */
    ApplicationUser findByEmail(String email);

    /**
     * Return all locked user accounts in the persistent datastore.
     *
     * @return all blocked users
     */
    List<ApplicationUser> findAllByLockedTrue();

    /**
     * Delete user by Email.
     *
     * @param email of user to delete
     */
    void deleteByEmail(String email);

    /**
     * Email of existing user.
     *
     * @param email of user
     */
    boolean existsByEmail(String email);

    /**
     * Activating a user.
     *
     * @param userId of user
     */
    @Modifying
    @Transactional
    @Query("UPDATE ApplicationUser a SET a.isActivated = true WHERE a.id = :userId")
    void activateUser(@Param("userId") Long userId);


    @Transactional
    @Query("SELECT u FROM ApplicationUser u WHERE u.id <> :id")
    Page<ApplicationUser> findAllByIdNot(Long id, Pageable pageable);
}