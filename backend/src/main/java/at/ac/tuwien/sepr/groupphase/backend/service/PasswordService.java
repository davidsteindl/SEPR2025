package at.ac.tuwien.sepr.groupphase.backend.service;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.OttDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordChangeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordResetDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;

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

    Long validateOtt(OttDto ottDto) throws IllegalArgumentException;

    void changePassword(PasswordChangeDto passwordChangeDto) throws NotFoundException, IllegalArgumentException;

}
