package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class PasswordChangeDto {

    @NotNull
    Long id;

    @NotNull
    String password;

    @NotNull
    String confirmPassword;

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
