package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password;


import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class PasswordChangeDto {

    @NotNull
    Long id;

    @NotNull
    String password;

    @NotNull
    String confirmPassword;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        PasswordChangeDto that = (PasswordChangeDto) o;
        return Objects.equals(password, that.password) && Objects.equals(confirmPassword, that.confirmPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(password, confirmPassword);
    }

    @Override
    public String toString() {
        return "PasswordChangeDto{"
            + "password='" + password + '\''
            + ", confirmPassword='" + confirmPassword + '\''
            + '}';
    }
}
