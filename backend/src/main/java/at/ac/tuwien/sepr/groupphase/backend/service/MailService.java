package at.ac.tuwien.sepr.groupphase.backend.service;

import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.LocalDateTime;


public interface MailService {


    /**
     * Function to send a password reset email.
     *
     * @param email the receiver email
     * @param passwordResetLink the link for the user to reset his/her password
     */
    void sendPasswordResetEmail(String email, String passwordResetLink, String time);

    /**
     * Function to send an account activation email.
     *
     * @param email the receiver email
     * @param link the link for the user to activate his/her account
     */
    void sendAccountActivationEmail(String email, String link, String time);

    /**
     * Function to send an email.
     *
     * @param to the receiver address
     * @param context the header of the email
     * @param text the main part of the email (Body)
     */
    void sendMail(String to, String context, String text);
}
