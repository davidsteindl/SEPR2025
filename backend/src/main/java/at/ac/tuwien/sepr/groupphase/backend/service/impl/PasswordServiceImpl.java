package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordResetDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordOtt;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.OttPasswordRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class PasswordServiceImpl implements PasswordService {

    MailService mailService;
    OneTimeTokenService oneTimeTokenService;
    UserService userService;
    OttPasswordRepository ottPasswordRepository;
    private Clock clock;

    public PasswordServiceImpl(MailService mailService, OneTimeTokenService oneTimeTokenService, UserService userService,
                               OttPasswordRepository ottPasswordRepository) {
        this.mailService = mailService;
        this.oneTimeTokenService = oneTimeTokenService;
        this.userService = userService;
        this.ottPasswordRepository = ottPasswordRepository;
    }

    @Override
    public void requestResetPassword(PasswordResetDto passwordResetDto) throws UsernameNotFoundException, IllegalArgumentException {
        String email;
        if (passwordResetDto.getEmail() == null) {
            throw new IllegalArgumentException("no email provided");
        } else {
            email = passwordResetDto.getEmail();
        }
        if (userService.findApplicationUserByEmail(email) == null) {
            throw new UsernameNotFoundException(email);
        }
        mailService.sendPasswordResetEmail(email, createOttLink(email, "/account/reset-password"));
    }

    private String createOttLink(String email, String relativePath) {
        if (userService.findApplicationUserByEmail(email).getId() == null) {
            throw new NotFoundException("email not found");
        }
        OneTimeToken oneTimeToken = oneTimeTokenService.generate(new GenerateOneTimeTokenRequest(email));

        PasswordOtt passwordOtt = new PasswordOtt();

        passwordOtt.setOttPassword(oneTimeToken.toString());
        passwordOtt.setConsumed(false);
        passwordOtt.setValidUntil(LocalDateTime.from(this.clock.instant().plusSeconds(300L)));
        passwordOtt.setUserId(userService.findApplicationUserByEmail(email).getId());

        ottPasswordRepository.save(passwordOtt);

        return ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path(relativePath)
            .queryParam("token", oneTimeToken.getTokenValue())
            .build()
            .toUriString();
    }


}
