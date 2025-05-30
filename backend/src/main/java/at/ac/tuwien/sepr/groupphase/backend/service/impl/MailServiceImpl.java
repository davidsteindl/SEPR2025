package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final JavaMailSender javaMailSender;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.javaMailSender = mailSender;
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String email, String passwordResetLink) {

        try {
            sendMail(email, passwordResetLink);
        } catch (MessagingException e) {
            LOGGER.error("Error while sending reset password email => {}", e.getLocalizedMessage());
        }
    }

    private void sendMail(String to, String text) throws MessagingException {
        LOGGER.info("Sending mail...");
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setFrom("noreply@gmail.com");
        message.setSubject("reset password for account");
        message.setText(text);

        javaMailSender.send(message);
    }


}
