package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.repository.OttPasswordRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class PasswordServiceImpl implements PasswordService {

    MailService mailService;
    OneTimeTokenService oneTimeTokenService;
    UserService userService;
    OttPasswordRepository ottPasswordRepository;

    public PasswordServiceImpl(MailService mailService, OneTimeTokenService oneTimeTokenService, UserService userService,
                               OttPasswordRepository ottPasswordRepository) {
        this.mailService = mailService;
        this.oneTimeTokenService = oneTimeTokenService;
        this.userService = userService;
        this.ottPasswordRepository = ottPasswordRepository;
    }

    public void requestResetPassword(String email) {
        if (userService.findApplicationUserByEmail(email) == null) {
            throw new UsernameNotFoundException(email);
        }
        mailService.sendPasswordResetEmail(email, createOttLink(email, "/account/reset-password"));
    }

    private String createOttLink(String email, String relativePath) {
        OneTimeToken oneTimeToken = oneTimeTokenService.generate(new GenerateOneTimeTokenRequest(email));

        return ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path(relativePath)
            .queryParam("token", oneTimeToken.getTokenValue())
            .build()
            .toUriString();
    }


}
