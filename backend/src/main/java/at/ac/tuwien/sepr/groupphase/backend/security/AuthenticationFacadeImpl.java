package at.ac.tuwien.sepr.groupphase.backend.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {
    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder
            .getContext()
            .getAuthentication();
    }

    @Override
    public Long getCurrentUserId() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        try {
            return Long.valueOf(principal.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }


    @Override
    public boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        return auth != null
            && auth.isAuthenticated()
            && !(auth instanceof AnonymousAuthenticationToken);
    }


    @Override
    public boolean hasRole(String role) {
        Authentication auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
            .anyMatch(gr -> gr.getAuthority().equals(role));
    }
}
