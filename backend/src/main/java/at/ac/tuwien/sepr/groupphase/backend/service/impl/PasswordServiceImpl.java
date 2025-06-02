package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.OttDto;
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
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;

@Service
public class PasswordServiceImpl implements PasswordService {

    private final UserRepository userRepository;
    MailService mailService;
    OneTimeTokenService oneTimeTokenService;
    UserService userService;
    OtTokenRepository otTokenRepository;
    private final UserValidator userValidator;

    public PasswordServiceImpl(MailService mailService, OneTimeTokenService oneTimeTokenService, UserService userService,
                               OtTokenRepository otTokenRepository, UserRepository userRepository, UserValidator userValidator) {
        this.mailService = mailService;
        this.oneTimeTokenService = oneTimeTokenService;
        this.userService = userService;
        this.otTokenRepository = otTokenRepository;
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    @Override
    public void requestResetPassword(PasswordResetDto passwordResetDto) throws NotFoundException, IllegalArgumentException {

        String email;
        if (passwordResetDto.getEmail() == null) {
            throw new IllegalArgumentException("no email provided");
        } else {
            email = passwordResetDto.getEmail();
        }
        if (userService.findApplicationUserByEmail(email) == null) {
            throw new NotFoundException(email);
        }
        mailService.sendPasswordResetEmail(email, createOttLink(email, "/account/reset-password"));
    }

    @Override
    public void validateOtt(OttDto ottDto) throws IllegalArgumentException {

        Long userId = otTokenRepository.findUserIdByOtToken(ottDto.getOtToken());

        if (userId == null) {
            throw new IllegalArgumentException("One-Time-Token is wrong");
        } else {
            otTokenRepository.markConsumed(userId);
            ottDto.setUserId(userId);
        }

    }

    @Override
    public void changePassword(PasswordChangeDto passwordChangeDto) throws NotFoundException, ValidationException {

        userValidator.validateForPasswordChange(passwordChangeDto);

        ApplicationUser user = userService.findUserById(passwordChangeDto.getId());
        if (user == null) {
            throw new NotFoundException("No User found");
        }
        updateUser(user, passwordChangeDto);
    }

    private void updateUser(ApplicationUser user, PasswordChangeDto passwordChangeDto) {
        user.setPassword(passwordChangeDto.getPassword());

        userRepository.save(user);
    }


    private String createOttLink(String email, String relativePath) {
        if (userService.findApplicationUserByEmail(email).getId() == null) {
            throw new NotFoundException("email not found");
        }
        OneTimeToken oneTimeToken = oneTimeTokenService.generate(new GenerateOneTimeTokenRequest(email));

        PasswordOtt passwordOtt = new PasswordOtt();

        passwordOtt.setOtToken(oneTimeToken.toString());
        passwordOtt.setConsumed(false);
        passwordOtt.setValidUntil(LocalDateTime.now().plusSeconds(300L));
        passwordOtt.setUserId(userService.findApplicationUserByEmail(email).getId());

        otTokenRepository.save(passwordOtt);

        return ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .port(4200)
            .path(relativePath)
            .queryParam("token", oneTimeToken.getTokenValue())
            .build()
            .toUriString();
    }


}
