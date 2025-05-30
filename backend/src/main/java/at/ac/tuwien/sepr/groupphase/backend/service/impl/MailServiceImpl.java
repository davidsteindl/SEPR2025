package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
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
        sendMail(email, "reset password for account", passwordResetLink);
    }

    public void sendMail(String to, String context, String text) {
        LOGGER.info("Sending mail...");
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setFrom("noreply@gmail.com");
        message.setSubject(context);
        message.setText(text);

        javaMailSender.send(message);
    }


}
