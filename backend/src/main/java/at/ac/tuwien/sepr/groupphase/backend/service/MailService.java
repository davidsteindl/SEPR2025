package at.ac.tuwien.sepr.groupphase.backend.service;

import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.JavaMailSender;


public interface MailService {


    /**
     * Function to send a password reset email.
     *
     * @param email the receiver email
     * @param passwordResetLink the link for the user to reset his/her password
     */
    void sendPasswordResetEmail(String email, String passwordResetLink);

    void sendMail(String to, String text) throws MessagingException;
}
