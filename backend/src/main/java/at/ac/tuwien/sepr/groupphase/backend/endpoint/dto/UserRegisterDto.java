package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private LocalDateTime dateOfBirth;

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    private boolean isLocked;
    private boolean isAdmin;
    private int loginTries;

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

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDateTime dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public int getLoginTries() {
        return loginTries;
    }

    public void setLoginTries(int loginTries) {
        this.loginTries = loginTries;
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
            && Objects.equals(dateOfBirth, userRegisterDto.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dateOfBirth, email, password, confirmPassword);
    }

    @Override
    public String toString() {
        return "UserRegisterDto{"
            + "Firstname='" + firstName + '\''
            + "Lastname='" + lastName + '\''
            + "DateofBirth='" + dateOfBirth + '\''
            + "email='" + email + '\''
            + ", password='" + password + '\''
            + '}';
    }

    public static final class UserRegisterDtoBuilder {

        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String email;
        private String password;
        private String confirmPassword;


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

        public UserRegisterDto.UserRegisterDtoBuilder withConfirmPassword(String password) {
            this.password = password;
            return this;
        }

        public UserRegisterDto build() {
            UserRegisterDto userRegisterDto = new UserRegisterDto();
            userRegisterDto.setFirstName(firstName);
            userRegisterDto.setLastName(lastName);
            userRegisterDto.setDateOfBirth(dateOfBirth.atStartOfDay());
            userRegisterDto.setEmail(email);
            userRegisterDto.setPassword(password);
            userRegisterDto.setConfirmPassword(confirmPassword);
            userRegisterDto.setLocked(false);
            userRegisterDto.setAdmin(false);
            userRegisterDto.setLoginTries(0);
            return userRegisterDto;
        }
    }

}
