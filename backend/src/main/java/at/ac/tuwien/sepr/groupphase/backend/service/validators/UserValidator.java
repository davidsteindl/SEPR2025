package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );


    public UserValidator() {
    }

    /**
     * The method to validate an UserRegisterDto for the registration.
     *
     * @param userRegisterDto the information for the new user
     * @throws ValidationException if a Validation is wrong
     */
    public void validateForRegistration(UserRegisterDto userRegisterDto) throws ValidationException {

        if (userRegisterDto.getFirstName() == null || userRegisterDto.getFirstName().isEmpty()) {
            throw new ValidationException("First name is required");
        }

        if (userRegisterDto.getFirstName().length() >= 100) {
            throw new ValidationException("First name is too long");
        }

        if (userRegisterDto.getLastName() == null) {
            throw new ValidationException("Last name is required");
        }

        if (userRegisterDto.getLastName().length() >= 100) {
            throw new  ValidationException("Last name is too long");
        }

        if (!isValidEmail(userRegisterDto.getEmail())) {
            throw new ValidationException("Email is not valid");
        }

        if (userRegisterDto.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new ValidationException("The Birthdate must be in the past");
        }

        if (userRegisterDto.getPassword() == null || userRegisterDto.getPassword().length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }

        if (userRegisterDto.getConfirmPassword() == null || userRegisterDto.getConfirmPassword().length() < 8) {
            throw new ValidationException("ConfirmPassword must be at least 8 characters");
        }

        if (!userRegisterDto.getPassword().equals(userRegisterDto.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        if (!userRegisterDto.getTermsAccepted()) {
            throw new ValidationException("Terms and Condition must be accepted");
        }
    }


    /**
     * The method to check if the email is valid via Pattern.
     *
     * @param email the email to be checked
     * @return true or false - depends on if the email is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
}
