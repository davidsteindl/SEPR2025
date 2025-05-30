package at.ac.tuwien.sepr.groupphase.backend.service;

import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.JavaMailSender;


public interface MailService {

    /**
     * Function to send an email.
     *
     * @param to the receiver email
     * @param subject what the email is about (header)
     * @param text what is the content of the email (body)
     */
    void sendMail(String to, String subject, String text) throws MessagingException;


    /**
     * Function to send a password reset email.
     *
     * @param email the receiver email
     * @param passwordResetLink the link for the user to reset his/her password
     */
    void sendPasswordResetEmail(String email, String passwordResetLink);
}
