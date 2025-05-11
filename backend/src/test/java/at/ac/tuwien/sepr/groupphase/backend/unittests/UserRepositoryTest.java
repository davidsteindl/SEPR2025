package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDate;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void givenUser_whenSaved_thenCanBeRetrievedByEmail() {
        ApplicationUser user = ApplicationUser.ApplicationUserBuilder.aUser()
            .withEmail("test@example.com")
            .withPassword("securepassword")
            .withFirstName("Max")
            .withLastName("Mustermann")
            .withSex(Sex.MALE)
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .isLocked(false)
            .isAdmin(false)
            .withLoginTries(0)
            .build();

        userRepository.save(user);

        ApplicationUser retrieved = userRepository.findByEmail("test@example.com");

        assertNotNull(retrieved);
        assertEquals("Max", retrieved.getFirstName());
    }


    @Test
    void givenUser_whenDeletedByEmail_thenUserShouldBeRemoved() {
        ApplicationUser user = ApplicationUser.ApplicationUserBuilder.aUser()
            .withEmail("delete@example.com")
            .withPassword("securepassword")
            .withFirstName("Max")
            .withLastName("Mustermann")
            .withSex(Sex.MALE)
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .isLocked(false)
            .isAdmin(false)
            .withLoginTries(0)
            .build();

        userRepository.save(user);
        assertNotNull(userRepository.findByEmail("delete@example.com"));

        userRepository.deleteByEmail("delete@example.com");

        assertNull(userRepository.findByEmail("delete@example.com"));
    }

    @Test
    void existingUser_shouldBeFoundByEmail() {
        ApplicationUser user = ApplicationUser.ApplicationUserBuilder.aUser()
            .withEmail("existing@example.com")
            .withPassword("securepassword")
            .withFirstName("Max")
            .withLastName("Mustermann")
            .withSex(Sex.MALE)
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .isLocked(false)
            .isAdmin(false)
            .withLoginTries(0)
            .build();

        userRepository.save(user);
        assertTrue(userRepository.existsByEmail("existing@example.com"));

    }

}
