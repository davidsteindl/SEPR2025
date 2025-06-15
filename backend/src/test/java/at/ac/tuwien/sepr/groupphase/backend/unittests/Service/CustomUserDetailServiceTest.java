package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.message.SimpleMessageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.MessageMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import at.ac.tuwien.sepr.groupphase.backend.exception.LoginAttemptException;
import at.ac.tuwien.sepr.groupphase.backend.repository.MessageRepository;
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
import java.util.ArrayList;
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
    private MessageRepository messageRepository;

    @Mock
    private MessageMapper messageMapper;

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
            .withViewedMessages(new ArrayList<>())
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

        assertTrue(testUser.isLocked());
        assertEquals("Your account is locked due to too many failed login attempts, please contact an administrator",
            exception.getMessage());
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
    void getUnseenMessages_returnsCorrectDtos() {
        Long userId = 1L;

        Message m1 = Message.MessageBuilder.aMessage()
            .withId(10L)
            .withTitle("Title1")
            .withSummary("Summary1")
            .withText("Text1")
            .withViewers(new ArrayList<>())
            .build();

        Message m2 = Message.MessageBuilder.aMessage()
            .withId(20L)
            .withTitle("Title2")
            .withSummary("Summary2")
            .withText("Text2")
            .withViewers(new ArrayList<>())
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(messageRepository.findAllUnseenByUserIdOrderByPublishedAtDesc(userId))
            .thenReturn(List.of(m1, m2));

        when(messageMapper.messageToSimpleMessageDto(m1)).thenReturn(
            SimpleMessageDto.SimpleMessageDtoBuilder.aSimpleMessageDto()
                .withId(m1.getId())
                .withTitle(m1.getTitle())
                .withSummary(m1.getSummary())
                .build()
        );

        when(messageMapper.messageToSimpleMessageDto(m2)).thenReturn(
            SimpleMessageDto.SimpleMessageDtoBuilder.aSimpleMessageDto()
                .withId(m2.getId())
                .withTitle(m2.getTitle())
                .withSummary(m2.getSummary())
                .build()
        );

        List<SimpleMessageDto> dtos = userDetailService.getUnseenMessages(userId);

        assertAll(
            () -> assertEquals(2, dtos.size()),
            () -> {
                SimpleMessageDto d1 = dtos.get(0);
                assertEquals(10L, d1.getId());
                assertEquals("Title1", d1.getTitle());
                assertEquals("Summary1", d1.getSummary());
            },
            () -> {
                SimpleMessageDto d2 = dtos.get(1);
                assertEquals(20L, d2.getId());
                assertEquals("Title2", d2.getTitle());
                assertEquals("Summary2", d2.getSummary());
            }
        );

        verify(messageRepository).findAllUnseenByUserIdOrderByPublishedAtDesc(userId);
        verify(messageMapper).messageToSimpleMessageDto(m1);
        verify(messageMapper).messageToSimpleMessageDto(m2);
    }


    @Test
    void markMessagesAsSeen_marksMessagesAndSavesUser() {
        Long userId = 1L;
        List<Long> messageIds = List.of(10L, 20L);
        Message m1 = Message.MessageBuilder.aMessage().withId(10L).withViewers(new ArrayList<>()).build();
        Message m2 = Message.MessageBuilder.aMessage().withId(20L).withViewers(new ArrayList<>()).build();

        ApplicationUser user = testUser;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(messageRepository.findAllById(messageIds)).thenReturn(List.of(m1, m2));

        userDetailService.markMessagesAsSeen(userId, messageIds);

        assertTrue(user.getViewedMessages().containsAll(List.of(m1, m2)));
        assertTrue(m1.getViewers().contains(user));
        assertTrue(m2.getViewers().contains(user));

        verify(userRepository).save(user);
    }
}
