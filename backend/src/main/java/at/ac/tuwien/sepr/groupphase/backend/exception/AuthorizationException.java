package at.ac.tuwien.sepr.groupphase.backend.exception;

/**
 * Exception that signals, that user tried to export a document that is not authorized
 * for the currently loged in session.
 */
public class AuthorizationException extends RuntimeException {

    public AuthorizationException(String message) {
        super(message);
    }
}
