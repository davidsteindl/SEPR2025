package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser.ApplicationUserBuilder;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Profile("generateData")
@Component
public class UserDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String USER_EMAIL = "user@email.com";
    private static final String ADMIN_EMAIL = "admin@email.com";

    private static final String PASSWORD = "password";

    @Autowired
    public UserDataGenerator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    private void generateUsers() {
        if (userRepository.findAll().size() > 0) {
            LOGGER.debug("users already generated");
        } else {
            LOGGER.debug("generating users");
            ApplicationUser admin = ApplicationUserBuilder.aUser()
                    .withEmail(ADMIN_EMAIL)
                    .withPassword(passwordEncoder.encode(PASSWORD))
                    .withFirstName("Max")
                    .withLastName("Mustermann")
                    .withDateOfBirth(LocalDate.of(1972, 2, 3))
                    .isLocked(false)
                    .isAdmin(true)
                    .withLoginTries(0)
                    .build();
            userRepository.save(admin);

            ApplicationUser user = ApplicationUserBuilder.aUser()
                    .withEmail(USER_EMAIL)
                    .withPassword(passwordEncoder.encode(PASSWORD))
                    .withFirstName("Magdalena")
                    .withLastName("Musterfrau")
                    .withDateOfBirth(LocalDate.of(1980, 7, 15))
                    .isLocked(false)
                    .isAdmin(false)
                    .withLoginTries(0)
                    .build();
            userRepository.save(user);
        }
    }
}
