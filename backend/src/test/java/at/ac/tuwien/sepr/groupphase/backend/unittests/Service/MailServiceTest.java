package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.OtTokenRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EventLocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.GetMapping;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class MailServiceTest {

    @Autowired
    private OtTokenRepository otTokenRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private JavaMailSender javaMailSender;




    @Test
    public void testMail() {
        mailService.sendPasswordResetEmail("test@example.com", "https://example.com/reset-password/token123");

    }
}
