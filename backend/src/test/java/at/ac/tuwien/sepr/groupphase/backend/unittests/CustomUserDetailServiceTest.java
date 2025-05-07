package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.LoginAttemptException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.CustomUserDetailService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.UserValidator;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserValidator userValidator;

    @Mock
    private JwtTokenizer jwtTokenizer;

    @InjectMocks
    private CustomUserDetailService userDetailService;

    private ApplicationUser testUser;
    private UserRegisterDto testUserInvalidCauseFirstNameTooLong;
    private UserRegisterDto testUserInvalidCauseFirstNameMissing;
    private UserRegisterDto testUserInvalidCauseLastNameTooLong;
    private UserRegisterDto testUserInvalidCauseLastNameMissing;
    private UserRegisterDto testUserInvalidCauseEmail;
    private UserRegisterDto testUserInvalidCauseFutureDate;
    private UserRegisterDto testUserInvalidConfirmPasswordTooShort;
    private UserRegisterDto testUserInvalidPasswordsNotEqual;
    private UserRegisterDto testUserInvalidCheckBoxUnticked;
    private UserRegisterDto testUserInvalidCausePasswordTooShort;
    private UserRegisterDto testUserValid;
    private UserRegisterDto testUserEmpty;

    @BeforeEach
    void setUp() {
        testUser = ApplicationUser.ApplicationUserBuilder.aUser()

            .withEmail("user@email.com")
            .withPassword("encodedPassword")
            .withFirstName("Test")
            .withLastName("User")
            .withDateOfBirth(LocalDate.of(1980, 1, 1))
            .isLocked(false)
            .isAdmin(false)
            .withLoginTries(0)
            .build();

        testUserInvalidCauseFirstNameTooLong = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("A".repeat(101))
            .withLastName("ValidLastName")
            .withEmail("valid@example.com")
            .withPassword("ValidPass123")
            .withConfirmPassword("ValidPass123")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withTermsAccepted(true)
            .build();

        testUserInvalidCauseFirstNameMissing = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName(null)
            .withLastName("ValidLastName")
            .withEmail("valid@example.com")
            .withPassword("ValidPass123")
            .withConfirmPassword("ValidPass123")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withTermsAccepted(true)
            .build();

        testUserInvalidCauseLastNameTooLong = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("ValidFirstName")
            .withLastName("B".repeat(101))
            .withEmail("valid@example.com")
            .withPassword("ValidPass123")
            .withConfirmPassword("ValidPass123")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withTermsAccepted(true)
            .build();

        testUserInvalidCauseLastNameMissing = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("ValidFirstName")
            .withLastName(null)
            .withEmail("valid@example.com")
            .withPassword("ValidPass123")
            .withConfirmPassword("ValidPass123")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withTermsAccepted(true)
            .build();

        testUserInvalidCauseEmail = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("Valid")
            .withLastName("User")
            .withEmail("invalidemail")
            .withPassword("ValidPass123")
            .withConfirmPassword("ValidPass123")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withTermsAccepted(true)
            .build();

        testUserInvalidCauseFutureDate = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("Valid")
            .withLastName("User")
            .withEmail("valid@example.com")
            .withPassword("ValidPass123")
            .withConfirmPassword("ValidPass123")
            .withDateOfBirth(LocalDate.now().plusDays(1))
            .withTermsAccepted(true)
            .build();

        testUserInvalidCheckBoxUnticked = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("Valid")
            .withLastName("User")
            .withEmail("valid@example.com")
            .withPassword("ValidPass123")
            .withConfirmPassword("ValidPass123")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withTermsAccepted(false)
            .build();

        testUserInvalidPasswordsNotEqual = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("Valid")
            .withLastName("User")
            .withEmail("valid@example.com")
            .withPassword("ValidPass123")
            .withConfirmPassword("DifferentPass123")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withTermsAccepted(true)
            .build();

        testUserInvalidCausePasswordTooShort = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("Valid")
            .withLastName("User")
            .withEmail("valid@example.com")
            .withPassword("short")
            .withConfirmPassword("short")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withTermsAccepted(true)
            .build();

        testUserInvalidConfirmPasswordTooShort = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("Valid")
            .withLastName("User")
            .withEmail("valid@example.com")
            .withPassword("short")
            .withConfirmPassword("short")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withTermsAccepted(true)
            .build();

        testUserValid = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("Valid")
            .withLastName("User")
            .withEmail("valid@example.com")
            .withPassword("ValidPass")
            .withConfirmPassword("ValidPass")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withTermsAccepted(true)
            .build();

        testUserEmpty = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("")
            .withLastName("")
            .withEmail("")
            .withPassword("")
            .withConfirmPassword("")
            .withDateOfBirth(null)
            .withTermsAccepted(false)
            .build();


    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByEmail("user@email.com")).thenReturn(testUser);

        var userDetails = userDetailService.loadUserByUsername("user@email.com");

        assertAll(
            () -> assertEquals("user@email.com", userDetails.getUsername()),
            () -> assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")))
        );
        verify(userRepository).findByEmail("user@email.com");
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail("unknown@email.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userDetailService.loadUserByUsername("unknown@email.com"));
    }

    @Test
    void login_SuccessfulLoginResetsLoginTries() {
        when(userRepository.findByEmail("user@email.com")).thenReturn(testUser);
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtTokenizer.getAuthToken(any(), any())).thenReturn("jwt-token");

        UserLoginDto loginDto = UserLoginDto.UserLoginDtoBuilder.anUserLoginDto()
            .withEmail("user@email.com")
            .withPassword("password")
            .build();
        String token = userDetailService.login(loginDto);

        assertAll(
            () -> assertEquals("jwt-token", token),
            () -> assertEquals(0, testUser.getLoginTries())
        );
        verify(userRepository).save(testUser);
    }

    @Test
    void login_IncorrectPasswordIncrementsLoginTries() {
        when(userRepository.findByEmail("user@email.com")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        UserLoginDto loginDto = UserLoginDto.UserLoginDtoBuilder.anUserLoginDto()
            .withEmail("user@email.com")
            .withPassword("wrongPassword")
            .build();

        LoginAttemptException exception = assertThrows(LoginAttemptException.class,
            () -> userDetailService.login(loginDto));

        assertAll(
            () -> assertEquals("Username or password is incorrect", exception.getMessage()),
            () -> assertEquals(1, testUser.getLoginTries())
        );
        verify(userRepository).save(testUser);
    }

    @Test
    void login_LockAccountAfterMaxTries() {
        testUser.setLoginTries(4);

        when(userRepository.findByEmail("user@email.com")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        UserLoginDto loginDto = UserLoginDto.UserLoginDtoBuilder.anUserLoginDto()
            .withEmail("user@email.com")
            .withPassword("wrongPassword")
            .build();

        LoginAttemptException exception = assertThrows(LoginAttemptException.class,
            () -> userDetailService.login(loginDto));

        assertAll(
            () -> assertTrue(testUser.isLocked()),
            () -> assertEquals("Your account is locked due to too many failed login attempts, please contact an administrator",
                exception.getMessage())
        );
        verify(userRepository).save(testUser);
    }

    @Test
    void login_LoginAttemptWithLockedAccount() {
        testUser.setLocked(true);

        when(userRepository.findByEmail("user@email.com")).thenReturn(testUser);

        UserLoginDto loginDto = UserLoginDto.UserLoginDtoBuilder.anUserLoginDto()
            .withEmail("user@email.com")
            .withPassword("password")
            .build();

        LoginAttemptException exception = assertThrows(LoginAttemptException.class,
            () -> userDetailService.login(loginDto));

        assertEquals("Your account is locked due to too many failed login attempts, please contact an administrator",
            exception.getMessage());
        verify(userRepository, never()).save(testUser);
    }

    @Test
    void getLockedUsers_Success() {
        ApplicationUser u1 = ApplicationUser.ApplicationUserBuilder.aUser()
            .withId(1L)
            .withFirstName("A")
            .withLastName("One")
            .withEmail("a@one.com")
            .isLocked(true)
            .build();
        ApplicationUser u2 = ApplicationUser.ApplicationUserBuilder.aUser()
            .withId(2L)
            .withFirstName("B")
            .withLastName("Two")
            .withEmail("b@two.com")
            .isLocked(true)
            .build();

        when(userRepository.findAllByLockedTrue()).thenReturn(List.of(u1, u2));

        var dtos = userDetailService.getLockedUsers();

        assertAll(
            () -> assertEquals(2, dtos.size()),
            () -> {
                var d1 = dtos.get(0);
                assertAll(
                    () -> assertEquals(1L, d1.getId()),
                    () -> assertEquals("A", d1.getFirstName()),
                    () -> assertEquals("One", d1.getLastName()),
                    () -> assertEquals("a@one.com", d1.getEmail())
                );
            },
            () -> {
                var d2 = dtos.get(1);
                assertAll(
                    () -> assertEquals(2L, d2.getId()),
                    () -> assertEquals("B", d2.getFirstName()),
                    () -> assertEquals("Two", d2.getLastName()),
                    () -> assertEquals("b@two.com", d2.getEmail())
                );
            }
        );

        verify(userRepository).findAllByLockedTrue();
    }

    @Test
    void getLockedUsers_EmptyList() {
        when(userRepository.findAllByLockedTrue()).thenReturn(List.of());

        var dtos = userDetailService.getLockedUsers();

        assertTrue(dtos.isEmpty());
        verify(userRepository).findAllByLockedTrue();
    }

    @Test
    void unlockUser_Success() {
        testUser.setLocked(true);
        testUser.setLoginTries(5);
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));

        userDetailService.unlockUser(10L);

        assertAll(
            () -> assertFalse(testUser.isLocked()),
            () -> assertEquals(0, testUser.getLoginTries())
        );
        verify(userRepository).save(testUser);
    }

    @Test
    void unlockUser_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> userDetailService.unlockUser(99L));
        verify(userRepository, never()).save(any());
    }


    @Test
    void validateUserWithTooLongFirstNameThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserInvalidCauseFirstNameTooLong));
    }

    @Test
    void validateUserWithMissingFirstNameThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserInvalidCauseFirstNameMissing));
    }

    @Test
    void validateUserWithTooLongLastNameThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserInvalidCauseLastNameTooLong));
    }

    @Test
    void validateUserWithMissingLastNameThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserInvalidCauseLastNameMissing));
    }

    @Test
    void validateUserWithInvalidEmailThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserInvalidCauseEmail));
    }

    @Test
    void validateUserWithFutureBirthDateThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserInvalidCauseFutureDate));
    }

    @Test
    void validateUserWithTooShortPasswordThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserInvalidCausePasswordTooShort));
    }

    @Test
    void validateUserWithTooShortConfirmPasswordThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserInvalidConfirmPasswordTooShort));
    }

    @Test
    void validateUserWithPasswordsNotEqualThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserInvalidPasswordsNotEqual));
    }

    @Test
    void validateUserCheckBoxUntickedThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserInvalidCheckBoxUnticked));
    }

    @Test
    void validateEmptyUserThrowsException() {
        assertThrows(ValidationException.class, () -> userDetailService.register(testUserEmpty));
    }

    @Test
    void validateValidUserDoesNotThrowException() {
        assertDoesNotThrow(() -> userDetailService.register(testUserValid));
    }
}
