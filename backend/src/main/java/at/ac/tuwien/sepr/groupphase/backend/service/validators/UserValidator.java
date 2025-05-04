package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserValidator {

    public UserValidator() {
    }


    public void validateForRegistration(ApplicationUser user) throws ValidationException {


        if (!user.getEmail().contains("@")) {
            throw new ValidationException("The email must contain a @");
        }

        if (user.getDateOfBirth().isAfter(LocalDateTime.now())) {
            throw new ValidationException("The Birthdate must be in the past");
        }

    }
}
