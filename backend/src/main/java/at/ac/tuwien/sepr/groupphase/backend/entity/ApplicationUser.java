package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, name = "dateOfBirth")
    private LocalDateTime dateOfBirth;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private boolean isLocked;

    @Column(nullable = false, length = 100)
    private boolean isAdmin;

    @Column(nullable = false, length = 100)
    private int loginTries;

    public ApplicationUser() {
    }

    public ApplicationUser(String email, String password, Boolean isAdmin) {
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public ApplicationUser(String firstName, String lastName, LocalDateTime dateOfBirth, String email, String password, Boolean isAdmin) {
        this.name = firstName + " " + lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;  //TODO missing Hashing!
        this.isLocked = false;
        this.isAdmin = isAdmin;
        this.loginTries = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDateTime dateOfBirth) {
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
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApplicationUser applicationUser)) {
            return false;
        }
        return Objects.equals(id, applicationUser.id)
            && Objects.equals(name, applicationUser.name)
            && Objects.equals(dateOfBirth, applicationUser.dateOfBirth)
            && Objects.equals(email, applicationUser.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dateOfBirth, email);
    }

    @Override
    public String toString() {
        return "Message{"
            + "id=" + id
            + ", name=" + name
            + ", dateOfBirth='" + dateOfBirth + '\''
            + ", email='" + email + '\''
            + '}';
    }
}
