package at.ac.tuwien.sepr.groupphase.backend.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A class representing a Password-Reset One Time Token.
 */
@Getter
@Setter
@Entity
public class PasswordOtt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private LocalDateTime validUntil;

    @NotNull
    private boolean consumed;

}
