package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordOtt;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.OtTokenRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.TokenLinkService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenLinkServiceImpl implements TokenLinkService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    OtTokenRepository otTokenRepository;

    public TokenLinkServiceImpl(OtTokenRepository otTokenRepository, UserRepository userRepository) {

        this.otTokenRepository = otTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String createOttLink(String email, String relativePath) {
        LOGGER.debug("creating One-Time-Token Link");
        ApplicationUser user = userRepository.findByEmail(email);
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

        return "http://localhost:4200/" + relativePath + "/" + token;
    }
}
