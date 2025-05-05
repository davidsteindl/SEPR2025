package at.ac.tuwien.sepr.groupphase.backend.entity;

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

    @Column(nullable = false, length = 100)
    private String email;

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
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApplicationUser applicationUser)) {
            return false;
        }
        return Objects.equals(id, applicationUser.id)
            && Objects.equals(firstName, applicationUser.firstName)
            && Objects.equals(lastName, applicationUser.lastName)
            && Objects.equals(dateOfBirth, applicationUser.dateOfBirth)
            && Objects.equals(email, applicationUser.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, dateOfBirth, email);
    }

    @Override
    public String toString() {
        return "Message{"
            + "id=" + id
            + ", firstName=" + firstName
            + ", lastName=" + lastName
            + ", dateOfBirth='" + dateOfBirth + '\''
            + ", email='" + email + '\''
            + '}';
    }

    public static final class ApplicationUserBuilder {
        private Long id;
        private String firstName;
        private String lastName;
        private String password;
        private LocalDate dateOfBirth;
        private String email;
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

        public ApplicationUserBuilder withEmail(String email) {
            this.email = email;
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
            user.setEmail(email);
            user.setLocked(isLocked);
            user.setAdmin(isAdmin);
            user.setLoginTries(loginTries);
            return user;
        }
    }
}
