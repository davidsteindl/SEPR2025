package at.ac.tuwien.sepr.groupphase.backend.service.impl;

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
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordServiceImpl implements PasswordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    MailService mailService;
    OneTimeTokenService oneTimeTokenService;
    UserService userService;
    OtTokenRepository otTokenRepository;
    private final UserValidator userValidator;

    public PasswordServiceImpl(MailService mailService, UserService userService,
                               OtTokenRepository otTokenRepository, UserRepository userRepository, UserValidator userValidator) {
        this.mailService = mailService;
        this.userService = userService;
        this.otTokenRepository = otTokenRepository;
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    @Override
    public void requestResetPassword(PasswordResetDto passwordResetDto) throws NotFoundException, IllegalArgumentException {
        LOGGER.info("Password Reset starting");
        String email = "";

        if (passwordResetDto.getEmail() == null) {
            throw new IllegalArgumentException("no email provided");
        } else {

            email = passwordResetDto.getEmail();
        }
        if (userService.findApplicationUserByEmail(email) == null) {
            throw new NotFoundException(email);
        }
        mailService.sendPasswordResetEmail(email, createOttLink(email, "reset-password"));
    }


    private void validateOtt(PasswordChangeDto passwordChangeDto) throws IllegalArgumentException {
        LOGGER.debug("Validating One-Time-Token");

        LOGGER.debug("Eingehender Token: {}", passwordChangeDto.getOtToken());
        otTokenRepository.findAll().forEach(o -> LOGGER.debug("DB: {}", o.getOtToken()));

        Long userId = otTokenRepository.findUserIdByOtToken(passwordChangeDto.getOtToken());

        if (userId == null) {
            throw new IllegalArgumentException("One-Time-Token is wrong");
        } else {

            Optional<PasswordOtt> maybeToken = otTokenRepository.findByOtTokenAndConsumedFalseAndValidUntilAfter(passwordChangeDto.getOtToken(), LocalDateTime.now());
            if (maybeToken.isEmpty()) {
                throw new IllegalArgumentException("Token is invalid or already used");
            }
            otTokenRepository.markConsumed(userId);
            passwordChangeDto.setUserId(userId);
        }

    }

    @Override
    public void changePassword(PasswordChangeDto passwordChangeDto) throws NotFoundException, ValidationException, IllegalArgumentException {
        LOGGER.debug("starting changing password");
        validateOtt(passwordChangeDto);

        userValidator.validateForPasswordChange(passwordChangeDto);

        ApplicationUser user = userService.findUserById(passwordChangeDto.getUserId());
        if (user == null) {
            throw new NotFoundException("No User found");
        }
        updateUser(user, passwordChangeDto);
    }

    private void updateUser(ApplicationUser user, PasswordChangeDto passwordChangeDto) {
        LOGGER.debug("Updating user");

        user.setPassword(passwordChangeDto.getPassword());
        userRepository.save(user);
    }


    private String createOttLink(String email, String relativePath) {
        ApplicationUser user = userService.findApplicationUserByEmail(email);
        if (user == null || user.getId() == null) {
            throw new NotFoundException("email not found");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime validUntil = LocalDateTime.now().plusMinutes(5);

        PasswordOtt ott = new PasswordOtt();
        ott.setUserId(user.getId());
        ott.setOtToken(token);
        ott.setValidUntil(validUntil);
        ott.setConsumed(false);
        otTokenRepository.save(ott);

        return  "http://localhost:4200/" + relativePath + "?token=" + token;
    }


}
