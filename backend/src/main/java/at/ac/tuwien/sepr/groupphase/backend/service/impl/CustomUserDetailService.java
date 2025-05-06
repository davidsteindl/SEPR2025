package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LockedUserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.LoginAttemptException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.UserValidator;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static at.ac.tuwien.sepr.groupphase.backend.config.SecurityConstants.MAX_LOGIN_TRIES;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class CustomUserDetailService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;
    private final UserValidator userValidator;

    @Autowired
    public CustomUserDetailService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenizer jwtTokenizer, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
        this.userValidator = userValidator;
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
    public String login(UserLoginDto userLoginDto) {
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
        user.setLoginTries(0);
        userRepository.save(user);

        List<String> roles = loadUserByUsername(user.getEmail())
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return jwtTokenizer.getAuthToken(user.getEmail(), roles);
    }

    @Override
    public void register(UserRegisterDto userRegisterDto) throws ValidationException {

        System.out.println("register-Service");

        if (!userRegisterDto.getPassword().equals(userRegisterDto.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        ApplicationUser user = ApplicationUser.ApplicationUserBuilder.aUser()
            .withFirstName(userRegisterDto.getFirstName())
            .withLastName(userRegisterDto.getLastName())
            .withDateOfBirth(userRegisterDto.getDateOfBirth())
            .withEmail(userRegisterDto.getEmail())
            .withPassword(passwordEncoder.encode(userRegisterDto.getPassword()))
            .withLoginTries(0)
            .isAdmin(false)
            .isLocked(false)
            .build();

        userValidator.validateForRegistration(user);

        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Their is already a user with that email.");
        }

        userRepository.save(user);
    }

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
    public void unlockUser(Long id) {
        ApplicationUser user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        user.setLocked(false);
        user.setLoginTries(0);
        userRepository.save(user);
    }

}
