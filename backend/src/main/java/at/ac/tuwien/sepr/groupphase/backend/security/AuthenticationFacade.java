package at.ac.tuwien.sepr.groupphase.backend.security;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {
    Authentication getAuthentication();

    Long getCurrentUserId();

    boolean isAuthenticated();

    boolean hasRole(String role);
}
