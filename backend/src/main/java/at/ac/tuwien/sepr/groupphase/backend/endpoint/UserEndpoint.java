package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LockedUserDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("api/v1/users")
public class UserEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserService userService;

    public UserEndpoint(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/locked")
    @Secured("ROLE_ADMIN")
    public List<LockedUserDto> getLockedUsers() {
        LOGGER.info("getLockedUsers()");
        return userService.getLockedUsers();
    }

    @PutMapping("/{id}/lock")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Void> unlockUser(@PathVariable Long id) {
        LOGGER.info("getLockedUsers()");
        userService.unlockUser(id);
        return ResponseEntity.noContent().build();
    }
}
