package at.ac.tuwien.sepr.groupphase.backend.config;


import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;


@Component
public class MagicLinkOneTimeTokenGeneration implements OneTimeTokenGenerationSuccessHandler {
    MailService mailService;
    OneTimeTokenGenerationSuccessHandler redirectHandler = new RedirectOneTimeTokenGenerationSuccessHandler("/account/ott-sent");

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(UrlUtils.buildFullRequestUrl(request))
            .replacePath(request.getContextPath())
            .replaceQuery(null)
            .fragment(null)
            .path("/login/ott")
            .queryParam("token", oneTimeToken.getTokenValue());
        // base_url/login/ott?token=<token>
        String magicLink = builder.toUriString();

        mailService.sendMail(oneTimeToken.getUsername(),
            "Your Spring Security One Time Token",
            """
            Use the following link to sign in into the application: <a href="%s">%s</a>
            """.formatted(magicLink, magicLink));
        try {
            this.redirectHandler.handle(request, response, oneTimeToken);
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

