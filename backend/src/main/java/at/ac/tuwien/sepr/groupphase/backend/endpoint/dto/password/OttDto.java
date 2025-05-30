package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password;

import jakarta.validation.constraints.NotNull;


import java.util.Objects;

public class OttDto {

    @NotNull
    String otToken;

    public String getOtToken() {
        return otToken;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OttDto ottDto = (OttDto) o;
        return Objects.equals(otToken, ottDto.otToken);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(otToken);
    }
}
