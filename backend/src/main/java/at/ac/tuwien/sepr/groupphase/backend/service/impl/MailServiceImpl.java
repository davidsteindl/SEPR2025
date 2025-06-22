package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final JavaMailSender javaMailSender;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.javaMailSender = mailSender;
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String email, String link, String time) {
        LOGGER.info("sending email for Password-Reset ...");
        String htmlContent = """
                <html>
                    <body>
                        <p>Dear Customer,</p>
                        <p>You forgot your password and want to reset it. No problem! Just click the button below to continue:</p>

                        <a href="%s" style="display: inline-block; padding: 10px 20px; font-size: 16px;
                            color: white; background-color: #007bff; text-decoration: none; border-radius: 5px;">
                            Reset Password
                        </a>
                        <p> The token is valid until %s </p>
                        <br>
                        <p>If the button doesn't work, you can also click this link: <br>
                        <a href="%s">%s</a></p>

                        <p>Best regards,<br/>Your TicketLine Team</p>
                    </body>
                </html>
            """.formatted(link, time, link, link);

        sendMail(email, "Reset password for your TicketLine-Account", htmlContent);
    }

    @Async
    @Override
    public void sendUserUnlockEmail(String email) {
        LOGGER.info("sending email for User unlock ...");
        String htmlContent = """
                <html>
                    <body>
                        <p>Dear Customer,</p>
                        <p>Your Account was unlocked. You can log in to your account again :)</p>

                        <p>Best regards,<br/>Your TicketLine Team</p>
                    </body>
                </html>
            """;

        sendMail(email, "Account unlocked for your TicketLine-Account", htmlContent);
    }

    @Async
    @Override
    public void sendUserBlockEmail(String email) {
        LOGGER.info("sending email for User block ...");
        String htmlContent = """
                <html>
                    <body>
                        <p>Dear Customer,</p>
                        <p>Your Account was blocked. You can no longer log in to your account.</p>

                        <p>Best regards,<br/>Your TicketLine Team</p>
                    </body>
                </html>
            """;

        sendMail(email, "Account blocked for your TicketLine-Account", htmlContent);
    }

    @Async
    @Override
    public void sendAccountActivationEmail(String email, String link, String time) {
        LOGGER.info("sending email for Account-Activation ...");
        String htmlContent = """
                <html>
                    <body>
                        <p>Dear Customer,</p>
                        <p>an Account was created for you. Please activate it by setting a password for your account.</p>

                        <a href="%s" style="display: inline-block; padding: 10px 20px; font-size: 16px;
                            color: white; background-color: #007bff; text-decoration: none; border-radius: 5px;">
                            Activate your Account
                        </a>
                        <br>
                        <p> The token is valid until %s </p>
                        <br>
                        <p>If the button doesn't work, you can also click this link: <br>
                        <a href="%s">%s</a></p>

                        <p>Best regards,<br/>Your TicketLine Team</p>
                    </body>
                </html>
           \s""".formatted(link, time,  link, link);

        sendMail(email, "Account Activation for your TicketLine-Account", htmlContent);
    }

    @Override
    public void sendMail(String to, String subject, String text) {
        LOGGER.info("Sending Html-mail to {}", to);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("no@reply.com");

            helper.setText(text, true); // true = HTML
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            LOGGER.error("Failed to send mail", e);
            throw new RuntimeException("Failed to send mail", e);
        }
    }


}
