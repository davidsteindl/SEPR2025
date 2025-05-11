package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
        List<String> validationErrors = new ArrayList<>();

        if (userRegisterDto.getFirstName() == null || userRegisterDto.getFirstName().isEmpty()) {
            validationErrors.add("First name is required");
        } else if (userRegisterDto.getFirstName().length() >= 100) {
            validationErrors.add("First name is too long");
        }

        if (userRegisterDto.getLastName() == null) {
            validationErrors.add("Last name is required");
        } else if (userRegisterDto.getLastName().length() >= 100) {
            validationErrors.add("Last name is too long");
        }

        if (userRegisterDto.getEmail() == null || !isValidEmail(userRegisterDto.getEmail())) {
            validationErrors.add("Email is not valid");
        }

        if (userRegisterDto.getDateOfBirth() == null || userRegisterDto.getDateOfBirth().isAfter(LocalDate.now())) {
            validationErrors.add("The Birthdate must be in the past");
        }

        if (userRegisterDto.getPassword() == null || userRegisterDto.getPassword().length() < 8) {
            validationErrors.add("Password must be at least 8 characters");
        }

        if (userRegisterDto.getConfirmPassword() == null || userRegisterDto.getConfirmPassword().length() < 8) {
            validationErrors.add("ConfirmPassword must be at least 8 characters");
        }

        if (!userRegisterDto.getPassword().equals(userRegisterDto.getConfirmPassword())) {
            validationErrors.add("Passwords do not match");
        }

        if (!userRegisterDto.getTermsAccepted()) {
            validationErrors.add("Terms and Condition must be accepted");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of user for registration failed", validationErrors);
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


    public void validateForUpdate(UserUpdateDto user) throws NotFoundException, ValidationException {
        List<String> validationErrors = new ArrayList<>();

        if (user.getEmail() != null && !user.getEmail().contains("@")) {
            validationErrors.add("The email must contain a @");
        }

        if (user.getDateOfBirth() != null && user.getDateOfBirth().isAfter(LocalDate.now())) {
            validationErrors.add("The Birthdate must be in the past");
        }

        if (user.getEmail() == null) {
            validationErrors.add("No Email found");
        }

        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            validationErrors.add("First name is required");
        } else if (user.getFirstName().length() >= 100) {
            validationErrors.add("First name is too long");
        }

        if (user.getLastName() == null) {
            validationErrors.add("Last name is required");
        } else if (user.getLastName().length() >= 100) {
            validationErrors.add("Last name is too long");
        }


        if (user.getFirstName() != null && !user.getFirstName().matches("^[a-zA-ZäöüÄÖÜ -]+$")) {
            validationErrors.add("First Name contains symbols");
        }


        if (user.getLastName() != null && !user.getLastName().matches("^[a-zA-ZäöüÄÖÜ -]+$")) {
            validationErrors.add("Last Name contains symbols");
        }

        if (user.getAddress() != null) {
            if (user.getAddress().isBlank()) {
                validationErrors.add("Address is given but blank");
            }
            if (user.getAddress().length() > 400) {
                validationErrors.add("Address is too long: longer than 400 characters");
            }
        }

        if (user.getPaymentData() != null) {
            if (user.getPaymentData().isBlank()) {
                validationErrors.add("Payment Data is given but blank");
            }
            if (user.getPaymentData().length() > 200) {
                validationErrors.add("Payment Data too long: longer than 200 characters");
            }
        }

        if (user.getSex() == null) {
            validationErrors.add("Sex must not be empty");
        }


        if (user.getDateOfBirth() == null) {
            validationErrors.add("No birthdate given");
        } else if (user.getDateOfBirth().isAfter(LocalDate.now())) {
            validationErrors.add("The Birthdate must be in the past");
        }


        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of user for update failed", validationErrors);
        }


    }

}