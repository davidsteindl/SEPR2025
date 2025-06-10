package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private Sex sex;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = true, length = 100)
    private String housenumber;

    @Column(nullable = true, length = 100)
    private String country;

    @Column(nullable = true, length = 100)
    private String city;

    @Column(nullable = true, length = 200)
    private String street;

    @Column(nullable = true, length = 100)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private boolean locked;

    @Column(nullable = false, length = 100)
    private boolean isAdmin;

    @Column(nullable = false, length = 100)
    private int loginTries;

    @Column(nullable = false)
    private boolean isActivated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public int getLoginTries() {
        return loginTries;
    }

    public void setLoginTries(int loginTries) {
        this.loginTries = loginTries;
    }


    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationUser that = (ApplicationUser) o;
        return locked == that.locked
            && isAdmin == that.isAdmin
            && loginTries == that.loginTries
            && id.equals(that.id)
            && firstName.equals(that.firstName)
            && lastName.equals(that.lastName)
            && password.equals(that.password)
            && dateOfBirth.equals(that.dateOfBirth)
            && sex == that.sex && email.equals(that.email)
            && Objects.equals(housenumber, that.housenumber)
            && Objects.equals(country, that.country)
            && Objects.equals(city, that.city)
            && Objects.equals(street, that.street)
            && Objects.equals(postalCode, that.postalCode);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + dateOfBirth.hashCode();
        result = 31 * result + sex.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + Objects.hashCode(housenumber);
        result = 31 * result + Objects.hashCode(country);
        result = 31 * result + Objects.hashCode(city);
        result = 31 * result + Objects.hashCode(street);
        result = 31 * result + Objects.hashCode(postalCode);
        result = 31 * result + Boolean.hashCode(locked);
        result = 31 * result + Boolean.hashCode(isAdmin);
        result = 31 * result + loginTries;
        return result;
    }

    @Override
    public String toString() {
        return "ApplicationUser{"
            + "id=" + id
            +  ", firstName='" + firstName + '\''
            + ", lastName='" + lastName + '\''
            + ", dateOfBirth=" + dateOfBirth
            + ", sex=" + sex
            + ", email='" + email + '\''
            + ", locked=" + locked
            + ", admin=" + isAdmin
            + ", loginTries=" + loginTries
            + '}';
    }

    public static final class ApplicationUserBuilder {
        private Long id;
        private String firstName;
        private String lastName;
        private String password;
        private LocalDate dateOfBirth;
        private Sex sex;
        private String email;
        private String city;
        private String street;
        private String postalCode;
        private String housenumber;
        private String country;
        private boolean isLocked;
        private boolean isAdmin;
        private int loginTries;
        private boolean isActivated;

        private ApplicationUserBuilder() {
        }

        public static ApplicationUserBuilder aUser() {
            return new ApplicationUserBuilder();
        }

        public ApplicationUserBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ApplicationUserBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public ApplicationUserBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public ApplicationUserBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public ApplicationUserBuilder withDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public ApplicationUserBuilder withSex(Sex sex) {
            this.sex = sex;
            return this;
        }

        public ApplicationUserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public ApplicationUserBuilder withIsActivated(Boolean activated) {
            this.isActivated = activated;
            return this;
        }

        public ApplicationUserBuilder withCity(String city) {
            this.city = city;
            return this;
        }

        public ApplicationUserBuilder withStreet(String street) {
            this.street = street;
            return this;
        }

        public ApplicationUserBuilder withPostalCode(String postalCode) {
            this.postalCode = city;
            return this;
        }

        public ApplicationUserBuilder withHouseNumber(String housenumber) {
            this.housenumber = housenumber;
            return this;
        }

        public ApplicationUserBuilder withCountry(String country) {
            this.country = country;
            return this;
        }

        public ApplicationUserBuilder isLocked(boolean isLocked) {
            this.isLocked = isLocked;
            return this;
        }

        public ApplicationUserBuilder isAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }

        public ApplicationUserBuilder withLoginTries(int loginTries) {
            this.loginTries = loginTries;
            return this;
        }

        public ApplicationUser build() {
            ApplicationUser user = new ApplicationUser();
            user.setId(id);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(password);
            user.setDateOfBirth(dateOfBirth);
            user.setSex(sex);
            user.setEmail(email);
            user.setStreet(street);
            user.setPostalCode(postalCode);
            user.setHousenumber(housenumber);
            user.setCountry(country);
            user.setLocked(isLocked);
            user.setAdmin(isAdmin);
            user.setLoginTries(loginTries);
            user.setActivated(isActivated);
            return user;
        }
    }
}
