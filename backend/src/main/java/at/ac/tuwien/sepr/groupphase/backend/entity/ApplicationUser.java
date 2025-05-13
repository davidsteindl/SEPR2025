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

    @Column(nullable = true)
    private Sex sex;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = true, length = 200)
    private String address;

    @Column(nullable = true, length = 200)
    private String paymentData;

    @Column(nullable = false, length = 100)
    private boolean locked;

    @Column(nullable = false, length = 100)
    private boolean admin;

    @Column(nullable = false, length = 100)
    private int loginTries;

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
        return admin;
    }

    public void setAdmin(Boolean isAdmin) {
        this.admin = isAdmin;
    }

    public int getLoginTries() {
        return loginTries;
    }

    public void setLoginTries(int loginTries) {
        this.loginTries = loginTries;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicationUser that = (ApplicationUser) o;
        return locked == that.locked && admin == that.admin && loginTries == that.loginTries
            && Objects.equals(id, that.id) && Objects.equals(firstName, that.firstName)
            && Objects.equals(lastName, that.lastName) && Objects.equals(password, that.password)
            && Objects.equals(dateOfBirth, that.dateOfBirth) && sex == that.sex
            && Objects.equals(email, that.email) && Objects.equals(address, that.address)
            && Objects.equals(paymentData, that.paymentData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, password, dateOfBirth, sex, email, address, paymentData, locked, admin, loginTries);
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
            + ", address='" + address + '\''
            + ", locked=" + locked
            + ", admin=" + admin
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
        private String address;
        private String paymentData;
        private boolean isLocked;
        private boolean isAdmin;
        private int loginTries;

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

        public ApplicationUserBuilder withAddress(String address) {
            this.address = address;
            return this;
        }

        public ApplicationUserBuilder withPaymentData(String paymentData) {
            this.paymentData = paymentData;
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
            user.setAddress(address);
            user.setPaymentData(paymentData);
            user.setLocked(isLocked);
            user.setAdmin(isAdmin);
            user.setLoginTries(loginTries);
            return user;
        }
    }
}
