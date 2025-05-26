package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import java.time.LocalDate;
import java.util.Objects;

public class UserDetailDto {

    private String email;
    private String firstName;
    private String lastName;
    private Sex sex;
    private String housenumber;
    private String country;
    private String city;
    private String street;
    private String postalCode;
    private String password;
    private String confirmPassword;
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

    public String getHousenumber() {
        return housenumber;
    }

    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserDetailDto that = (UserDetailDto) o;
        return isLocked == that.isLocked
            && isAdmin == that.isAdmin
            && loginTries == that.loginTries
            && Objects.equals(email, that.email)
            && Objects.equals(firstName, that.firstName)
            && Objects.equals(lastName, that.lastName)
            && sex == that.sex
            && Objects.equals(housenumber, that.housenumber)
            && Objects.equals(country, that.country)
            && Objects.equals(city, that.city)
            && Objects.equals(street, that.street)
            && Objects.equals(postalCode, that.postalCode)
            && Objects.equals(password, that.password)
            && Objects.equals(confirmPassword, that.confirmPassword)
            && Objects.equals(dateOfBirth, that.dateOfBirth);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(email);
        result = 31 * result + Objects.hashCode(firstName);
        result = 31 * result + Objects.hashCode(lastName);
        result = 31 * result + Objects.hashCode(sex);
        result = 31 * result + Objects.hashCode(housenumber);
        result = 31 * result + Objects.hashCode(country);
        result = 31 * result + Objects.hashCode(city);
        result = 31 * result + Objects.hashCode(street);
        result = 31 * result + Objects.hashCode(postalCode);
        result = 31 * result + Objects.hashCode(password);
        result = 31 * result + Objects.hashCode(confirmPassword);
        result = 31 * result + Objects.hashCode(dateOfBirth);
        result = 31 * result + Boolean.hashCode(isLocked);
        result = 31 * result + Boolean.hashCode(isAdmin);
        result = 31 * result + loginTries;
        return result;
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



}
