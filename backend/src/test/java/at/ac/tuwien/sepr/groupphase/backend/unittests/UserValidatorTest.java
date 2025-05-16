package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserValidatorTest {

  private UserValidator userValidator;

  @BeforeEach
  void setUp() {
    userValidator = new UserValidator();
  }

  @Test
  void validateUserWithTooLongFirstNameThrowsException() {
    UserRegisterDto dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("A".repeat(101))
        .withLastName("ValidLastName")
        .withEmail("valid@example.com")
        .withPassword("ValidPass123")
        .withConfirmPassword("ValidPass123")
        .withDateOfBirth(LocalDate.of(1990, 1, 1))
        .withTermsAccepted(true)
        .build();

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userValidator.validateForRegistration(dto);
    });

    assertEquals("Validation of user for registration failed. Failed validations: First name is too long.", exception.getMessage());
  }

  @Test
  void validateUserWithMissingFirstNameThrowsException() {
    UserRegisterDto dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName(null)
        .withLastName("ValidLastName")
        .withEmail("valid@example.com")
        .withPassword("ValidPass123")
        .withConfirmPassword("ValidPass123")
        .withDateOfBirth(LocalDate.of(1990, 1, 1))
        .withTermsAccepted(true)
        .build();

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userValidator.validateForRegistration(dto);
    });

    assertEquals("Validation of user for registration failed. Failed validations: First name is required.", exception.getMessage());
  }

  @Test
  void validateUserWithTooLongLastNameThrowsException() {
    UserRegisterDto dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("ValidFirstName")
        .withLastName("B".repeat(101))
        .withEmail("valid@example.com")
        .withPassword("ValidPass123")
        .withConfirmPassword("ValidPass123")
        .withDateOfBirth(LocalDate.of(1990, 1, 1))
        .withTermsAccepted(true)
        .build();

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userValidator.validateForRegistration(dto);
    });

    assertEquals("Validation of user for registration failed. Failed validations: Last name is too long.", exception.getMessage());
  }

  @Test
  void validateUserWithMissingLastNameThrowsException() {
    UserRegisterDto  dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("ValidFirstName")
        .withLastName(null)
        .withEmail("valid@example.com")
        .withPassword("ValidPass123")
        .withConfirmPassword("ValidPass123")
        .withDateOfBirth(LocalDate.of(1990, 1, 1))
        .withTermsAccepted(true)
        .build();

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userValidator.validateForRegistration(dto);
    });

    assertEquals("Validation of user for registration failed. Failed validations: Last name is required.", exception.getMessage());
  }

  @Test
  void validateUserWithInvalidEmailThrowsException() {
    UserRegisterDto dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("Valid")
        .withLastName("User")
        .withEmail("invalidemail")
        .withPassword("ValidPass123")
        .withConfirmPassword("ValidPass123")
        .withDateOfBirth(LocalDate.of(1990, 1, 1))
        .withTermsAccepted(true)
        .build();

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userValidator.validateForRegistration(dto);
    });

    assertEquals("Validation of user for registration failed. Failed validations: Email is not valid.", exception.getMessage());
  }

  @Test
  void validateUserWithFutureBirthDateThrowsException() {
    UserRegisterDto dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("Valid")
        .withLastName("User")
        .withEmail("valid@example.com")
        .withPassword("ValidPass123")
        .withConfirmPassword("ValidPass123")
        .withDateOfBirth(LocalDate.now().plusDays(1))
        .withTermsAccepted(true)
        .build();

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userValidator.validateForRegistration(dto);
    });

    assertEquals("Validation of user for registration failed. Failed validations: The Birthdate must be in the past.", exception.getMessage());
  }

  @Test
  void validateUserWithTooShortPasswordThrowsException() {
    UserRegisterDto dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("Valid")
        .withLastName("User")
        .withEmail("valid@example.com")
        .withPassword("short")
        .withConfirmPassword("short")
        .withDateOfBirth(LocalDate.of(1990, 1, 1))
        .withTermsAccepted(true)
        .build();

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userValidator.validateForRegistration(dto);
    });

    assertEquals("Validation of user for registration failed. Failed validations: Password must be at least 8 characters, ConfirmPassword must be at least 8 characters.", exception.getMessage());
  }

  @Test
  void validateUserWithTooShortConfirmPasswordThrowsException() {
    UserRegisterDto dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("Valid")
        .withLastName("User")
        .withEmail("valid@example.com")
        .withPassword("tooshort")
        .withConfirmPassword("short")
        .withDateOfBirth(LocalDate.of(1990, 1, 1))
        .withTermsAccepted(true)
        .build();

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userValidator.validateForRegistration(dto);
    });

    assertEquals("Validation of user for registration failed. Failed validations: ConfirmPassword must be at least 8 characters, Passwords do not match.", exception.getMessage());
  }

  @Test
  void validateUserWithPasswordsNotEqualThrowsException() {
    UserRegisterDto dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("Valid")
        .withLastName("User")
        .withEmail("valid@example.com")
        .withPassword("ValidPass123")
        .withConfirmPassword("DifferentPass123")
        .withDateOfBirth(LocalDate.of(1990, 1, 1))
        .withTermsAccepted(true)
        .build();

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userValidator.validateForRegistration(dto);
    });

    assertEquals("Validation of user for registration failed. Failed validations: Passwords do not match.", exception.getMessage());
  }

  @Test
  void validateUserCheckBoxUntickedThrowsException() {
    UserRegisterDto dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("Valid")
        .withLastName("User")
        .withEmail("valid@example.com")
        .withPassword("ValidPass123")
        .withConfirmPassword("ValidPass123")
        .withDateOfBirth(LocalDate.of(1990, 1, 1))
        .withTermsAccepted(false)
        .build();

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userValidator.validateForRegistration(dto);
    });

    assertEquals("Validation of user for registration failed. Failed validations: Terms and Condition must be accepted.", exception.getMessage());
  }

  @Test
  void validateEmptyUserThrowsException() {
    UserRegisterDto  dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("")
        .withLastName("")
        .withEmail("")
        .withPassword("")
        .withConfirmPassword("")
        .withDateOfBirth(null)
        .withTermsAccepted(false)
        .build();


    assertThrows(ValidationException.class, () -> userValidator.validateForRegistration(dto));
  }

  @Test
  void validateValidUserDoesNotThrowException() {
    UserRegisterDto dto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
        .withFirstName("Valid")
        .withLastName("User")
        .withEmail("valid@example.com")
        .withPassword("ValidPass")
        .withConfirmPassword("ValidPass")
        .withDateOfBirth(LocalDate.of(1990, 1, 1))
        .withTermsAccepted(true)
        .build();

    assertDoesNotThrow(() -> userValidator.validateForRegistration(dto));
  }

  @Test
  void validateUpdateWithAcceptedUserData() {
    UserUpdateDto dto = new UserUpdateDto();
    dto.setFirstName("Valid");
    dto.setLastName("User");
    dto.setEmail("valid@example.com");
    dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
    dto.setSex(Sex.FEMALE);
    dto.setHousenumber("13");
    dto.setPostalCode("New PostalCode");
    dto.setStreet("New Street");
    dto.setCity("Vienna");
    dto.setCountry("Austria");

    assertDoesNotThrow(() -> userValidator.validateForUpdate(dto));
  }

  @Test
  void validateUpdateWithInvalidUserData() throws ValidationException {
    UserUpdateDto dto = new UserUpdateDto();
    dto.setFirstName("Valid");
    dto.setLastName("User");
    dto.setEmail("valid@example.com");
    dto.setDateOfBirth(LocalDate.of(2030, 1, 1));
    dto.setSex(Sex.FEMALE);
    dto.setHousenumber("13");
    dto.setPostalCode("New PostalCode");
    dto.setStreet("New Street");
    dto.setCity("Vienna");
    dto.setCountry("Austria");

    assertThrows(ValidationException.class, () -> userValidator.validateForUpdate(dto));

  }


}