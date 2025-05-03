package at.ac.tuwien.sepr.groupphase.backend.exception;

import org.springframework.security.core.AuthenticationException;

public class LoginAttemptException extends AuthenticationException {
    private final int loginTries;

    public LoginAttemptException(String msg, int loginTries) {
        super(msg);
        this.loginTries = loginTries;
    }

    public int getLoginTries() {
        return loginTries;
    }
}