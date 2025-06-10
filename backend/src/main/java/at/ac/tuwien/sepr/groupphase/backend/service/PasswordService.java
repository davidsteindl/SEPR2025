package at.ac.tuwien.sepr.groupphase.backend.service;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordChangeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordResetDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

/**
 * Service for a Password-Reset.
 *
 */
public interface PasswordService {

    /**
     * Method to request a password reset.
     *
     * @param passwordResetDto the receiver email
     */
    void requestResetPassword(PasswordResetDto passwordResetDto) throws NotFoundException, IllegalArgumentException;


    /**
     * Method to change a User-Password.
     *
     * @param passwordChangeDto the User with his/her new password and the One-time-Token
     */
    void changePassword(PasswordChangeDto passwordChangeDto) throws NotFoundException, ValidationException, IllegalArgumentException;


}
