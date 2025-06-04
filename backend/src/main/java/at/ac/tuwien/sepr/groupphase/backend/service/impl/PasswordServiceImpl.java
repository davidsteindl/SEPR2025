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
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordServiceImpl implements PasswordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    MailService mailService;
    OneTimeTokenService oneTimeTokenService;
    UserService userService;
    OtTokenRepository otTokenRepository;

    public PasswordServiceImpl(MailService mailService, UserService userService, PasswordEncoder passwordEncoder,
                               OtTokenRepository otTokenRepository, UserRepository userRepository, UserValidator userValidator) {
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.otTokenRepository = otTokenRepository;
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    @Override
    public void requestResetPassword(PasswordResetDto passwordResetDto) throws NotFoundException, IllegalArgumentException {
        LOGGER.debug("Password Reset starting");
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

    @Override
    public void changePassword(PasswordChangeDto passwordChangeDto) throws NotFoundException, ValidationException {
        LOGGER.debug("starting changing password");
        validateOtt(passwordChangeDto);

        userValidator.validateForPasswordChange(passwordChangeDto);

        ApplicationUser user = userService.findUserById(passwordChangeDto.getUserId());
        if (user == null) {
            throw new NotFoundException("No User found");
        }
        updateUser(user, passwordChangeDto);
        otTokenRepository.markConsumed(passwordChangeDto.getOtToken());
    }

    /**
     * Method to validate the one-time-token from the user.
     *
     * @param passwordChangeDto the DTO which has the one-time-token
     * @throws ValidationException If the validation of the token failed (invalid or already used)
     */
    private void validateOtt(PasswordChangeDto passwordChangeDto) throws ValidationException {
        LOGGER.debug("Validating One-Time-Token");
        List<String> validationErrors = new ArrayList<>();
        Long userId = otTokenRepository.findUserIdByOtTokenIfValid(passwordChangeDto.getOtToken());


        if (String.valueOf(userId) == null) {
            validationErrors.add("One-Time-Token is invalid or already used");
        } else {

            Optional<PasswordOtt> maybeToken =
                otTokenRepository.findByOtTokenAndConsumedFalseAndValidUntilAfter(passwordChangeDto.getOtToken(), LocalDateTime.now());
            if (maybeToken.isEmpty()) {
                validationErrors.add("One-Time-Token is invalid or already used");
            }
            if (!validationErrors.isEmpty()) {
                throw new ValidationException("Validation of Token failed", validationErrors);
            } else {

                passwordChangeDto.setUserId(userId);
            }
        }
    }

    /**
     * Method to Update the Password of the user.
     *
     * @param user the user who gets a new password
     * @param passwordChangeDto the new password
     */
    private void updateUser(ApplicationUser user, PasswordChangeDto passwordChangeDto) {
        LOGGER.debug("Updating user");

        user.setPassword(passwordEncoder.encode(passwordChangeDto.getPassword()));
        userRepository.save(user);
    }

    /**
     * Method to create the One-Time-Token Link for the User to click on in the email.
     *
     * @param email the email-address of the receiving person
     * @param relativePath the Path for the function which will be triggered
     * @return the Link for the email
     */
    private String createOttLink(String email, String relativePath) {
        LOGGER.debug("creating One-Time-Token Link");
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

        return "http://localhost:4200/" + relativePath + "?token=" + token;
    }


}
