package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordResetDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.OtTokenRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MailServiceTest {

    @Autowired
    private OtTokenRepository otTokenRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private JavaMailSender javaMailSender;


    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;



    @BeforeEach
    void setUp() {

        ApplicationUser testUser = ApplicationUser.ApplicationUserBuilder.aUser()
            .withEmail("markus@email.com")
            .withPassword("encodedPassword")
            .withFirstName("Test")
            .withLastName("User")
            .withSex(Sex.MALE)
            .withDateOfBirth(LocalDate.of(1980, 1, 1))
            .isLocked(false)
            .isAdmin(false)
            .withLoginTries(0)
            .build();

        userRepository.save(testUser);
    }




}
