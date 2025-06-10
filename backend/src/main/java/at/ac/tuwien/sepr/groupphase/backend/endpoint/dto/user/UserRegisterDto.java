package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Objects;

public class UserRegisterDto {

    @NotNull(message = "Firstname must not be null")
    private String firstName;

    @NotNull(message = "Lastname must not be null")
    private String lastName;

    @NotNull(message = "Password must not be null")
    private String password;

    @NotNull(message = "Password must not be null")
    private String confirmPassword;

    @NotNull(message =  "DateOfBirth must not be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotNull(message = "Email must not be null")
    private String email;

    @NotNull(message =  "TermsAccepted must not be null")
    private Boolean termsAccepted;

    @NotNull(message = "Sex must not be null")
    private Sex sex;

    @NotNull(message = "Admin must no be null")
    private Boolean isAdmin;


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Boolean getTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(Boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserRegisterDto userRegisterDto)) {
            return false;
        }
        return Objects.equals(email, userRegisterDto.email)
            && Objects.equals(password, userRegisterDto.password)
            && Objects.equals(confirmPassword, userRegisterDto.confirmPassword)
            && Objects.equals(firstName, userRegisterDto.firstName)
            && Objects.equals(lastName, userRegisterDto.lastName)
            && Objects.equals(dateOfBirth, userRegisterDto.dateOfBirth)
            && Objects.equals(termsAccepted, userRegisterDto.termsAccepted)
            && Objects.equals(sex, userRegisterDto.sex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dateOfBirth, email, password, sex, confirmPassword, termsAccepted);
    }

    @Override
    public String toString() {
        return "UserRegisterDto{"
            + "Firstname='" + firstName + '\''
            + "Lastname='" + lastName + '\''
            + "DateofBirth='" + dateOfBirth + '\''
            + "email='" + email + '\''
            + ", password='" + password + '\''
            + ", sex='" + sex + '\''
            + '}';
    }

    public static final class UserRegisterDtoBuilder {

        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String email;
        private String password;
        private String confirmPassword;
        private boolean termsAccepted;
        private Sex sex;


        private UserRegisterDtoBuilder() {
        }

        public static UserRegisterDto.UserRegisterDtoBuilder anUserRegisterDto() {
            return new UserRegisterDto.UserRegisterDtoBuilder();
        }

        public UserRegisterDto.UserRegisterDtoBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserRegisterDto.UserRegisterDtoBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserRegisterDto.UserRegisterDtoBuilder withDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public UserRegisterDto.UserRegisterDtoBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserRegisterDto.UserRegisterDtoBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public UserRegisterDto.UserRegisterDtoBuilder withConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
            return this;
        }

        public UserRegisterDto.UserRegisterDtoBuilder withTermsAccepted(boolean termsAccepted) {
            this.termsAccepted = termsAccepted;
            return this;
        }

        public UserRegisterDto.UserRegisterDtoBuilder withSex(Sex sex) {
            this.sex = sex;
            return this;
        }

        public UserRegisterDto build() {
            UserRegisterDto userRegisterDto = new UserRegisterDto();
            userRegisterDto.setFirstName(firstName);
            userRegisterDto.setLastName(lastName);
            userRegisterDto.setDateOfBirth(dateOfBirth);
            userRegisterDto.setEmail(email);
            userRegisterDto.setPassword(password);
            userRegisterDto.setConfirmPassword(confirmPassword);
            userRegisterDto.setTermsAccepted(termsAccepted);
            userRegisterDto.setSex(sex);
            return userRegisterDto;
        }
    }

}
