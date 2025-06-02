package at.ac.tuwien.sepr.groupphase.backend.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A class representing a Password-Reset One Time Token.
 */
@Entity
public class PasswordOtt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Long userId;

    @NotNull
    private String otToken;

    @NotNull
    private LocalDateTime validUntil;

    @NotNull
    private boolean consumed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public String getOtToken() {
        return otToken;
    }

    public void setOtToken(String otToken) {
        this.otToken = otToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PasswordOtt that = (PasswordOtt) o;
        return consumed == that.consumed && Objects.equals(id, that.id) && Objects.equals(userId, that.userId)
            && Objects.equals(otToken, that.otToken) && Objects.equals(validUntil, that.validUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, otToken, validUntil, consumed);
    }

    @Override
    public String toString() {
        return "PasswordOtt{"
            + "id=" + id
            + ", userId=" + userId
            + ", otToken='" + otToken + '\''
            + ", validUntil=" + validUntil
            + ", consumed=" + consumed
            + '}';
    }
}
