package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class UserDetailDto {

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    @NotNull(message = "Firstname must not be null")
    private String firstName;

    @NotNull(message = "Lastname must not be null")
    private String lastName;

    @NotNull(message = "Sex must not be null")
    private Sex sex;

    @NotNull(message = "Address must not be null")
    private String address;

    @NotNull(message = "payment Data must not be null")
    private String paymentData;

    @NotNull(message = "Password must not be null")
    private String password;

    @NotNull(message = "Password must not be null")
    private String confirmPassword;

    @NotNull(message =  "DateOfBirth must not be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentData() {
        return paymentData;
    }

    public void setPaymentData(String paymentData) {
        this.paymentData = paymentData;
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

    /*
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserDetailDto userDetailDto)) {
            return false;
        }
        return Objects.equals(email, userDetailDto.email)
            && Objects.equals(firstName, userDetailDto.firstName)
            && Objects.equals(lastName, userDetailDto.lastName)
            && Objects.equals(dateOfBirth, userDetailDto.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dateOfBirth, email, password, confirmPassword);
    }

    @Override
    public String toString() {
        return "UserDetailDto{"
            + "Firstname='" + firstName + '\''
            + "Lastname='" + lastName + '\''
            + "DateofBirth='" + dateOfBirth + '\''
            + "email='" + email + '\''
            + ", password='" + password + '\''
            + '}';
    }
    */



}
