package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

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

}