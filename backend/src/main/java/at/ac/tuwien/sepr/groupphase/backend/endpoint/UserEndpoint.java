package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LockedUserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.UserMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("api/v1/users")
public class UserEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserService userService;
    private final UserMapper userMapper;

    public UserEndpoint(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/locked")
    @Secured("ROLE_ADMIN")
    public List<LockedUserDto> getLockedUsers() {
        LOGGER.info("getLockedUsers()");
        return userService.getLockedUsers();
    }

    @PutMapping("/{id}/unlock")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Void> unlockUser(@PathVariable("id") Long id) {
        LOGGER.info("getLockedUsers()");
        userService.unlockUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get current User", security = @SecurityRequirement(name = "apiKey"))
    public UserDetailDto getCurrentUser() {
        LOGGER.info("getCurrentUser()");
        var email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        ApplicationUser user = this.userService.findApplicationUserByEmail(email);
        return userMapper.applicationUserToUserDetailDto(user);
    }

    @PutMapping("/me")
    @Secured("ROLE_USER")
    @Operation(summary = "Update current user", security = @SecurityRequirement(name = "apiKey"))
    public ResponseEntity<Void> updateCurrentUser(@RequestBody UserUpdateDto userUpdateDto) throws ValidationException {
        LOGGER.info("updateCurrentUser()");
        var email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        this.userService.update(email, userUpdateDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Secured("ROLE_USER")
    @Operation(summary = "Delete current User", security = @SecurityRequirement(name = "apiKey"))
    public ResponseEntity<Void> deleteUser() {
        LOGGER.info("deleteUser()");
        var email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        this.userService.delete(email);
        return ResponseEntity.noContent().build();
    }


}
