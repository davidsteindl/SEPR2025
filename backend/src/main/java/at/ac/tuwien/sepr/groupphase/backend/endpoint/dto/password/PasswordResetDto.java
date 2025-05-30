package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


public class PasswordResetDto {

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PasswordResetDto that = (PasswordResetDto) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }

    @Override
    public String toString() {
        return "PasswordResetDto{"
            + "email='" + email + '\''
            + '}';
    }
}
