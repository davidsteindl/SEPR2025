package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordChangeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordResetDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[^ ]+@[^ ]+.[^ ]+$"
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
        LOGGER.info("Validating user registration ...");
        List<String> validationErrors = new ArrayList<>();

        checkName(validationErrors, userRegisterDto.getFirstName(), userRegisterDto.getLastName());

        if (userRegisterDto.getEmail() == null || !isValidEmail(userRegisterDto.getEmail())) {
            validationErrors.add("Email is not valid");
        }

        if (userRegisterDto.getDateOfBirth() == null || userRegisterDto.getDateOfBirth().isAfter(LocalDate.now())) {
            validationErrors.add("The Birthdate must be in the past");
        }

        if (userRegisterDto.getDateOfBirth() == null || userRegisterDto.getDateOfBirth().isAfter(LocalDate.parse("2007-05-15"))) {
            validationErrors.add("You must be at least 18 years old to use the Service");
        }

        checkPassword(validationErrors, userRegisterDto.getPassword(), userRegisterDto.getConfirmPassword());

        if (!userRegisterDto.getTermsAccepted()) {
            validationErrors.add("Terms and Condition must be accepted");
        }

        if (userRegisterDto.getSex() == null) {
            validationErrors.add("Sex must be selected");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of user for registration failed", validationErrors);
        }


    }

    private void checkName(List<String> validationErrors, String firstName, String lastName) {
        if (firstName == null || firstName.isEmpty()) {
            validationErrors.add("First name is required");
        } else if (firstName.length() >= 100) {
            validationErrors.add("First name is too long");
        }

        if (lastName == null) {
            validationErrors.add("Last name is required");
        } else if (lastName.length() >= 100) {
            validationErrors.add("Last name is too long");
        }
    }

    /**
     * The method to check if the email is valid via Pattern.
     *
     * @param email the email to be checked
     * @return true or false - depends on if the email is valid
     */
    private static boolean isValidEmail(String email) {
        LOGGER.info("Validating email address ...");
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    public void validateForPasswordChange(PasswordChangeDto passwordChangeDto) throws ValidationException {
        LOGGER.info("Validating for Password-Change ...");
        List<String> validationErrors = new ArrayList<>();

        checkPassword(validationErrors, passwordChangeDto.getPassword(), passwordChangeDto.getConfirmPassword());

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation for Password Change failed", validationErrors);
        }


    }

    private void checkPassword(List<String> validationErrors, String password, String confirmPassword) {
        LOGGER.debug("Checking passwords ...");
        if (password == null || password.length() < 8) {
            validationErrors.add("Password must be at least 8 characters");
        }

        if (confirmPassword == null || confirmPassword.length() < 8) {
            validationErrors.add("ConfirmPassword must be at least 8 characters");
        }

        if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
            validationErrors.add("Passwords do not match");
        }
    }


    public void validateForUpdate(UserUpdateDto user) throws NotFoundException, ValidationException {
        LOGGER.info("Validating user update ...");
        List<String> validationErrors = new ArrayList<>();

        if (user.getEmail() == null || !isValidEmail(user.getEmail())) {
            validationErrors.add("Email is not valid");
        }

        if (user.getDateOfBirth() != null && user.getDateOfBirth().isAfter(LocalDate.now())) {
            validationErrors.add("The Birthdate must be in the past");
        }

        if (user.getEmail() == null) {
            validationErrors.add("No Email found");
        }

        checkName(validationErrors, user.getFirstName(), user.getLastName());

        if (user.getFirstName() != null && !user.getFirstName().matches("^[a-zA-ZäöüÄÖÜ -]+$")) {
            validationErrors.add("First Name contains symbols");
        }

        if (user.getLastName() != null && !user.getLastName().matches("^[a-zA-ZäöüÄÖÜ -]+$")) {
            validationErrors.add("Last Name contains symbols");
        }

        var addresscounter = 0;
        if (user.getStreet() != null) {
            if (user.getStreet().isBlank()) {
                validationErrors.add("Street is given but blank");
            }
            if (user.getStreet().length() > 200) {
                validationErrors.add("Street is too long: longer than 200 characters");
            }
            addresscounter++;
        }

        if (user.getCountry() != null) {
            if (user.getCountry().isBlank()) {
                validationErrors.add("Country is given but blank");
            }
            if (user.getCountry().length() > 100) {
                validationErrors.add("Country is too long: longer than 100 characters");
            }
            addresscounter++;
        }

        if (user.getCity() != null) {
            if (user.getCity().isBlank()) {
                validationErrors.add("City is given but blank");
            }
            if (user.getCity().length() > 100) {
                validationErrors.add("City is too long: longer than 100 characters");
            }
            addresscounter++;
        }

        if (user.getPostalCode() != null) {
            if (user.getPostalCode().isBlank()) {
                validationErrors.add("Postal Code is given but blank");
            }
            if (user.getPostalCode().length() > 100) {
                validationErrors.add("Postal Code is too long: longer than 100 characters");
            }
            addresscounter++;
        }

        if (user.getHousenumber() != null) {
            if (user.getHousenumber().isBlank()) {
                validationErrors.add("House Number is given but blank");
            }
            if (user.getHousenumber().length() > 100) {
                validationErrors.add("House Number is too long: longer than 100 characters");
            }
            addresscounter++;
        }

        if (addresscounter != 0 && addresscounter < 5) {
            validationErrors.add("Address is incomplete, fill out all required fields");
        }

        if (user.getSex() == null) {
            validationErrors.add("Sex must not be empty");
        }

        if (user.getDateOfBirth() == null) {
            validationErrors.add("No birthdate given");
        } else if (user.getDateOfBirth().isAfter(LocalDate.now())) {
            validationErrors.add("The Birthdate must be in the past");
        } else if (user.getDateOfBirth().isAfter(LocalDate.parse("2007-05-15"))) {
            validationErrors.add("You must be at least 18 years old to use the Service");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of user for update failed", validationErrors);
        }

    }

}