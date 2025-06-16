package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.message.SimpleMessageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordResetDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.LockedUserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.MessageMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.LoginAttemptException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.MessageRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordService;
import at.ac.tuwien.sepr.groupphase.backend.service.TokenLinkService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.UserValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static at.ac.tuwien.sepr.groupphase.backend.config.SecurityConstants.MAX_LOGIN_TRIES;

@Service
public class CustomUserDetailService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;
    private final JwtTokenizer jwtTokenizer;
    private final UserValidator userValidator;
    MailService mailService;
    TokenLinkService tokenLinkService;

    @Autowired
    public CustomUserDetailService(UserRepository userRepository, MessageRepository messageRepository, MessageMapper messageMapper,
                                   PasswordEncoder passwordEncoder,
                                   JwtTokenizer jwtTokenizer, UserValidator userValidator, MailService mailService,
                                   TokenLinkService tokenLinkService, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
        this.userValidator = userValidator;
        this.mailService = mailService;
        this.tokenLinkService = tokenLinkService;
        this.passwordService = passwordService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.debug("Load all user by email");
        try {
            ApplicationUser applicationUser = findApplicationUserByEmail(email);
            List<GrantedAuthority> grantedAuthorities;
            if (applicationUser.isAdmin()) {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");
            } else {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");
            }

            return new User(applicationUser.getEmail(), applicationUser.getPassword(), true, true, true,
                !applicationUser.isLocked(), grantedAuthorities);
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public ApplicationUser findApplicationUserByEmail(String email) {
        LOGGER.debug("Find application user by email");
        ApplicationUser applicationUser = userRepository.findByEmail(email);
        if (applicationUser != null) {
            return applicationUser;
        }
        throw new NotFoundException(String.format("Could not find the user with the email address %s", email));
    }

    @Override
    public ApplicationUser findUserById(Long id) {
        LOGGER.debug("Find user by id");
        Optional<ApplicationUser> applicationUser = userRepository.findById(id);
        if (applicationUser.isPresent()) {
            return applicationUser.get();
        }
        throw new NotFoundException(String.format("Could not find the user with the id %d", id));

    }

    @Override
    public String login(UserLoginDto userLoginDto) {
        LOGGER.debug("Login user by email");
        ApplicationUser user = userRepository.findByEmail(userLoginDto.getEmail());

        if (user == null) {
            throw new LoginAttemptException("Username or password is incorrect", 0);
        }

        if (user.isLocked()) {
            throw new LoginAttemptException(
                "Your account is locked due to too many failed login attempts, please contact an administrator",
                user.getLoginTries());
        }

        int currentTry = user.getLoginTries() + 1;

        if (!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())) { // login failed (password wrong)
            user.setLoginTries(currentTry);
            if (currentTry >= MAX_LOGIN_TRIES) {
                user.setLocked(true);
                userRepository.save(user);
                throw new LoginAttemptException(
                    "Your account is locked due to too many failed login attempts, please contact an administrator",
                    user.getLoginTries());
            }
            userRepository.save(user);
            throw new LoginAttemptException("Username or password is incorrect", user.getLoginTries());
        }

        if (!user.isActivated()) {
            throw new LoginAttemptException(
                "Your account is not yet activated, please look at your emails", user.getLoginTries());
        }
        user.setLoginTries(0);
        userRepository.save(user);

        List<String> roles = loadUserByUsername(user.getEmail())
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        return jwtTokenizer.getAuthToken(user.getId().toString(), roles);
    }

    @Override
    public void register(UserRegisterDto userRegisterDto) throws ValidationException {
        LOGGER.debug("Register user");

        userValidator.validateForRegistration(userRegisterDto);

        ApplicationUser user = ApplicationUser.ApplicationUserBuilder.aUser()
            .withFirstName(userRegisterDto.getFirstName())
            .withLastName(userRegisterDto.getLastName())
            .withDateOfBirth(userRegisterDto.getDateOfBirth())
            .withEmail(userRegisterDto.getEmail())
            .withPassword(passwordEncoder.encode(userRegisterDto.getPassword()))
            .withSex(userRegisterDto.getSex())
            .withLoginTries(0)
            .isAdmin(userRegisterDto.getIsAdmin())
            .isLocked(false)
            .withIsActivated(userRegisterDto.getIsActivated())
            .build();


        if (userRepository.existsByEmail(user.getEmail())) {
            List<String> validationErrors = new ArrayList<>();
            validationErrors.add("Email is already in use");
            throw new ValidationException("Validation of user for registration failed", validationErrors);
        }

        userRepository.save(user);

        if (!user.isActivated()) {
            LocalDateTime dateTime = LocalDateTime.now().plusMinutes(5);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

            String formatted = dateTime.format(formatter);
            this.mailService.sendAccountActivationEmail(user.getEmail(), tokenLinkService.createOttLink(user.getEmail(), "account-activation"), formatted);
        }
    }

    @Override
    public List<LockedUserDto> getLockedUsers() {
        LOGGER.debug("Fetching locked users");
        List<ApplicationUser> lockedUsers = userRepository.findAllByLockedTrue();

        return lockedUsers.stream()
            .map(user -> LockedUserDto.LockedUserDtoBuilder.aLockedUserDto()
                .withId(user.getId())
                .withFirstName(user.getFirstName())
                .withLastName(user.getLastName())
                .withEmail(user.getEmail())
                .build()
            )
            .toList();
    }

    @Override
    public Page<LockedUserDto> getAllUsersPaginated(Pageable pageable) {
        LOGGER.debug("Fetching all users paginated");
        return userRepository.findAll(pageable)
            .map(user -> LockedUserDto.LockedUserDtoBuilder.aLockedUserDto()
                .withId(user.getId())
                .withFirstName(user.getFirstName())
                .withLastName(user.getLastName())
                .withEmail(user.getEmail())
                .withIsLocked(user.isLocked())
                .build());
    }

    @Override
    public void unlockUser(Long id) {
        LOGGER.debug("unlock user {}", id);
        ApplicationUser user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        user.setLocked(false);
        user.setLoginTries(0);
        userRepository.save(user);
    }

    @Override
    public void blockUser(Long id) {
        LOGGER.debug("block user {}", id);
        ApplicationUser user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        user.setLocked(true);
        user.setLoginTries(0);
        userRepository.save(user);
    }

    @Override
    public void resetPassword(Long id) {
        LOGGER.debug("password Reset user {}", id);
        ApplicationUser user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setEmail(user.getEmail());
        passwordService.requestResetPassword(passwordResetDto);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        LOGGER.debug("Delete user {}", id);
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }


    @Transactional
    @Override
    public void update(Long id, UserUpdateDto userToUpdate) throws ValidationException {
        LOGGER.debug("Update user {}", id);
        var userInDatabase = userRepository.findById(id);

        if (userInDatabase.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        var user = userInDatabase.get();
        if (!user.getEmail().equals(userToUpdate.getEmail()) && userRepository.existsByEmail(userToUpdate.getEmail())) {
            throw new ConflictException("User with Email already exists");
        }

        userValidator.validateForUpdate(userToUpdate);

        user.setFirstName(userToUpdate.getFirstName());
        user.setLastName(userToUpdate.getLastName());
        user.setDateOfBirth(userToUpdate.getDateOfBirth());
        user.setEmail(userToUpdate.getEmail());
        user.setSex(userToUpdate.getSex());
        user.setPostalCode(userToUpdate.getPostalCode());
        user.setCity(userToUpdate.getCity());
        user.setCountry(userToUpdate.getCountry());
        user.setStreet(userToUpdate.getStreet());
        user.setHousenumber(userToUpdate.getHousenumber());
        user.setPostalCode(userToUpdate.getPostalCode());

        userRepository.save(user);
    }

    @Override
    public List<SimpleMessageDto> getUnseenMessages(Long userId) {
        LOGGER.debug("Get unseen messages for user {}", userId);
        ApplicationUser user = findUserById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return messageRepository.findAllUnseenByUserIdOrderByPublishedAtDesc(userId)
            .stream()
            .map(messageMapper::messageToSimpleMessageDto)
            .toList();
    }

    @Transactional
    @Override
    public void markMessageAsSeen(Long userId, Long messageId) {
        ApplicationUser user = findUserById(userId);
        Message message = messageRepository.findById(messageId).orElseThrow();

        if (!user.getViewedMessages().contains(message)) {
            user.getViewedMessages().add(message);
        }
        if (!message.getViewers().contains(user)) {
            message.getViewers().add(user);
        }

        userRepository.save(user);
    }
}
