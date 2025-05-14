package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.annotation.ValidNameCombination;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistDetailDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ArtistDetailNameValidator implements ConstraintValidator<ValidNameCombination, ArtistDetailDto> {

    @Override
    public boolean isValid(ArtistDetailDto value, ConstraintValidatorContext context) {
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