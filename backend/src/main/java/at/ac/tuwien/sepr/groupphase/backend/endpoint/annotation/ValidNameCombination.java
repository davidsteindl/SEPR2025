package at.ac.tuwien.sepr.groupphase.backend.endpoint.annotation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.validator.ArtistDetailNameValidator;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.validator.CreateArtistNameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {ArtistDetailNameValidator.class, CreateArtistNameValidator.class})
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNameCombination {
    String message() default "Either stagename or firstname and lastname must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
