package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.LoginAttemptException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.CustomUserDetailService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.UserValidator;
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


@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenizer jwtTokenizer;

    @InjectMocks
    private CustomUserDetailService userDetailService;

    private ApplicationUser testUser;


    @BeforeEach
    void setUp() {
        testUser = ApplicationUser.ApplicationUserBuilder.aUser()
            .withId(1L)
            .withEmail("user@email.com")
            .withPassword("encodedPassword")
            .withFirstName("Test")
            .withLastName("User")
            .withDateOfBirth(LocalDate.of(1980, 1, 1))
            .isLocked(false)
            .isAdmin(false)
            .withIsActivated(true)
            .withLoginTries(0)
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
            .withIsActivated(true)
            .build();
        ApplicationUser u2 = ApplicationUser.ApplicationUserBuilder.aUser()
            .withId(2L)
            .withFirstName("B")
            .withLastName("Two")
            .withEmail("b@two.com")
            .isLocked(true)
            .withIsActivated(true)
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
    void validateUserWithValidDataRegistersSuccessfully() throws ValidationException {
        UserRegisterDto validUser = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("Test")
            .withLastName("User")
            .withEmail("test@example.com")
            .withDateOfBirth(LocalDate.of(1990, 1, 1))
            .withPassword("abc12345")
            .withConfirmPassword("abc12345")
            .withTermsAccepted(true)
            .withIsActivated(true)
            .build();

        when(passwordEncoder.encode("abc12345")).thenReturn("encodedPassword");

        userDetailService.register(validUser);

        verify(userRepository).save(any(ApplicationUser.class));
    }


    @Test
    void delete_Success() {
        Long id = 3L;
        when(userRepository.existsById(id)).thenReturn(true);

        userDetailService.delete(id);

        verify(userRepository).deleteById(id);
    }


    @Test
    void delete_UserNotFound() {
        Long id = 3L;
        when(userRepository.existsById(id)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userDetailService.delete(id));
        verify(userRepository, never()).deleteById(any());
    }


    @Test
    void update_Success() throws ValidationException {
        Long id = 5L;
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setFirstName("NewFirstName");
        userUpdateDto.setLastName("NewLastName");
        userUpdateDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        userUpdateDto.setSex(Sex.MALE);
        userUpdateDto.setCountry("New Country");
        userUpdateDto.setStreet("New Street");
        userUpdateDto.setCity("New City");
        userUpdateDto.setPostalCode("New PostalCode");
        userUpdateDto.setHousenumber("15A");

        when(userRepository.findById(id)).thenReturn(Optional.of(testUser));
        doNothing().when(userValidator).validateForUpdate(userUpdateDto);

        userDetailService.update(id, userUpdateDto);

        assertAll(
            () -> assertEquals("NewFirstName", testUser.getFirstName()),
            () -> assertEquals("NewLastName", testUser.getLastName()),
            () -> assertEquals(LocalDate.of(1990, 1, 1), testUser.getDateOfBirth()),
            () -> assertEquals(Sex.MALE, testUser.getSex()),
            () -> assertEquals("New Country", testUser.getCountry()),
            () -> assertEquals("New Street", testUser.getStreet()),
            () -> assertEquals("New City", testUser.getCity()),
            () -> assertEquals("New PostalCode", testUser.getPostalCode()),
            () -> assertEquals("15A", testUser.getHousenumber())
        );
        verify(userRepository).save(testUser);
    }


    @Test
    void update_UserNotFound() {
        Long id = 9L;
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setFirstName("NewFirstName");
        userUpdateDto.setLastName("NewLastName");
        userUpdateDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        userUpdateDto.setSex(Sex.MALE);
        userUpdateDto.setCountry("New Country");
        userUpdateDto.setStreet("New Street");
        userUpdateDto.setCity("New City");
        userUpdateDto.setPostalCode("New PostalCode");
        userUpdateDto.setHousenumber("15A");

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userDetailService.update(id, userUpdateDto));
        verify(userRepository, never()).save(any());
    }

}
