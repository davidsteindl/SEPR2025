package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;


import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordChangeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordResetDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordOtt;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.OtTokenRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordService;
import at.ac.tuwien.sepr.groupphase.backend.service.TokenLinkService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.PasswordServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.UserValidator;
import jakarta.transaction.Transactional;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class PasswordServiceTest {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private MailService mailService; // Optional: mit @MockBean
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtTokenRepository ottTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenLinkService tokenService;

    @Autowired
    private UserValidator userValidator;

    private ApplicationUser testUser;
    private PasswordResetDto resetDtoValid;
    private PasswordResetDto resetDtoInvalid;
    private PasswordResetDto resetDtoInvalidTwo;
    private PasswordChangeDto changeDtoValid;
    private PasswordChangeDto changeDtoInvalid;
    private PasswordOtt passwordOtt;

    @BeforeEach
    void setup() {
        passwordService = new PasswordServiceImpl(mailService, passwordEncoder, ottTokenRepository, userRepository, userValidator, tokenService);

        userRepository.deleteAll();
        testUser = new ApplicationUser();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("test");
        testUser.setLastName("test");
        testUser.setPassword("Password");
        testUser.setDateOfBirth(LocalDate.now().minusYears(20));
        testUser.setSex(Sex.MALE);
        testUser.setCity("city");
        testUser.setPostalCode("1090");
        testUser.setCountry("Austria");
        testUser.setStreet("street");
        testUser = userRepository.save(testUser);

        resetDtoValid = new PasswordResetDto();
        resetDtoValid.setEmail("test@example.com");

        resetDtoInvalid = new PasswordResetDto();

        resetDtoInvalidTwo = new PasswordResetDto();
        resetDtoInvalidTwo.setEmail("fake@example.com");

        changeDtoValid = new PasswordChangeDto();
        changeDtoValid.setOtToken("valid-token");
        changeDtoValid.setPassword("newPassword");
        changeDtoValid.setConfirmPassword("newPassword");
        changeDtoValid.setUserId(testUser.getId());

        changeDtoInvalid = new PasswordChangeDto();
        changeDtoInvalid.setOtToken("invalid-token");
        changeDtoInvalid.setPassword("newPassword");
        changeDtoInvalid.setConfirmPassword("newPassword");
        changeDtoInvalid.setUserId(testUser.getId());

        ottTokenRepository.deleteAll();
        passwordOtt = new PasswordOtt();
        passwordOtt.setUserId(testUser.getId());
        passwordOtt.setOtToken("valid-token");
        passwordOtt.setValidUntil(LocalDateTime.now().plusMinutes(10));
        passwordOtt.setConsumed(false);
        ottTokenRepository.save(passwordOtt);
    }


    @Test
    void changePassword_validTokenAndUser_changesPassword() throws Exception {

        passwordService.changePassword(changeDtoValid);

        ApplicationUser updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("newPassword", updatedUser.getPassword()));

        Optional<PasswordOtt> deletedToken = ottTokenRepository.findByOtTokenAndConsumedFalseAndValidUntilAfter("valid-token",LocalDateTime.now());
        assertTrue(deletedToken.isEmpty());
    }

    @Test
    void changePassword_invalidToken_throwsValidationException() {

        assertThrows(ValidationException.class, ()-> passwordService.changePassword(changeDtoInvalid));

    }

    @Test
    void requestPassword_resetThrowsException_CauseNoEmailProvided() {

        assertThrows(IllegalArgumentException.class, () -> passwordService.requestResetPassword(resetDtoInvalid));
    }

    @Test
    void requestPassword_resetThrowsException_CauseWrongEmailProvided() {

        assertThrows(NotFoundException.class, () -> passwordService.requestResetPassword(resetDtoInvalidTwo));
    }

    @Test
    void createOttLink_withWrongEmail_throwsNotFoundException() {

        assertThrows(NotFoundException.class, () -> tokenService.createOttLink("fake@email.com","reset-password"));
    }

    @Test
    void createOttLink_Successful() {

        String link = tokenService.createOttLink("test@example.com", "reset-password");
        assertTrue(link.startsWith("http://localhost:4200/reset-password/"));
    }

}
