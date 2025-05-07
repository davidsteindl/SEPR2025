package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
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


    public void validateForRegistration(ApplicationUser user) throws ValidationException {

        if (user.getFirstName().length() >= 100) {
            throw new ValidationException("First name is required");
        }

        if (user.getLastName().length() >= 100) {
            throw new ValidationException("Last name is required");
        }

        if (!isValidEmail(user.getEmail())) {
            throw new ValidationException("Email is not valid");
        }

        if (user.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new ValidationException("The Birthdate must be in the past");
        }

        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }


    }



    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
}
