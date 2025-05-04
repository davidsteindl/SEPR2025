package at.ac.tuwien.sepr.groupphase.backend.config;

/**
 * Application-wide security constants.
 */
public class SecurityConstants {
    /** Maximum failed login attempts before locking an account. */
    public static final int MAX_LOGIN_TRIES = 5;

    private SecurityConstants() {
    }
}
