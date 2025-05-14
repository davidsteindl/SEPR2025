package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.annotation.ValidNameCombination;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreateArtistDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CreateArtistNameValidator implements ConstraintValidator<ValidNameCombination, CreateArtistDto> {

    @Override
    public boolean isValid(CreateArtistDto value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean hasStageName = value.getStagename() != null && !value.getStagename().isBlank();
        boolean hasFirstAndLastName =
            value.getFirstname() != null && !value.getFirstname().isBlank()
                && value.getLastname() != null && !value.getLastname().isBlank();

        return hasStageName || hasFirstAndLastName;
    }
}