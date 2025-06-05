package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;

import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
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


    @Autowired
    public UserDataGenerator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    private void generateUsers() {
        if (!userRepository.findAll().isEmpty()) {
            LOGGER.debug("Users already generated");
        } else {
            LOGGER.debug("Generating 50 admins and 1000 customers");
            for (int i = 1; i <= 50; i++) {
                String email = String.format("admin" + i + "@email.com");
                String firstName = "AdminFirstName" + i;
                String lastName = "AdminLastName" + i;
                Sex sex = (i % 2 == 0) ? Sex.FEMALE : Sex.MALE;
                LocalDate dob = LocalDate.of(1970, 1, 1).plusDays(i);

                ApplicationUser admin = ApplicationUserBuilder.aUser()
                    .withEmail(email)
                    .withPassword(passwordEncoder.encode("password" + i))
                    .withFirstName(firstName)
                    .withLastName(lastName)
                    .withDateOfBirth(dob)
                    .withSex(sex)
                    .isLocked(false)
                    .isAdmin(true)
                    .withLoginTries(0)
                    .build();
                userRepository.save(admin);
            }

            for (int i = 1; i <= 1000; i++) {
                String email = String.format("user" + i + "@email.com");
                String firstName = "UserFirstName" + i;
                String lastName = "UserLastName" + i;

                Sex sex = (i % 2 == 0) ? Sex.FEMALE : Sex.MALE;
                LocalDate dob = LocalDate.of(1990, 1, 1).plusDays(i);

                ApplicationUser customer = ApplicationUserBuilder.aUser()
                    .withEmail(email)
                    .withPassword(passwordEncoder.encode("password" + i))
                    .withFirstName(firstName)
                    .withLastName(lastName)
                    .withDateOfBirth(dob)
                    .withSex(sex)
                    .isLocked(false)
                    .isAdmin(false)
                    .withLoginTries(0)
                    .build();
                userRepository.save(customer);
            }

            LOGGER.debug("Finished generating 50 admins and 1000 customers");
        }
    }
}
