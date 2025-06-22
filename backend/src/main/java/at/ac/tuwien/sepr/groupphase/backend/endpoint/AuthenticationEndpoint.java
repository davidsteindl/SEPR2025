package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordChangeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.password.PasswordResetDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;


@RestController
@RequestMapping(value = "/api/v1/authentication")
public class AuthenticationEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserService userService;
    private final PasswordService passwordService;


    public AuthenticationEndpoint(UserService userService, PasswordService passwordService) {
        this.userService = userService;
        this.passwordService = passwordService;
    }

    /**
     * Method to get a UserLoginDto and login the user with it.
     *
     * @param userLoginDto the information for the user to log in
     * @return An AuthenticationToken
     */
    @PermitAll
    @PostMapping("/login")
    public String login(@Valid @RequestBody UserLoginDto userLoginDto) {
        return userService.login(userLoginDto);
    }

    /**
     * Method to get a UserRegisterDto and register a new User with it.
     *
     * @param userRegisterDto the Information for the new user
     * @return a ResponseEntity with the HTTP-Status for the frontend
     */
    @PermitAll
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody UserRegisterDto userRegisterDto) throws ValidationException {
        LOGGER.debug("register a new user: {}", userRegisterDto);
        userService.register(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PermitAll
    @PostMapping("/password-change-requests")
    public ResponseEntity<Void> passwordReset(@RequestBody PasswordResetDto passwordResetDto) {
        passwordService.requestResetPassword(passwordResetDto);
        return ResponseEntity.ok().build();
    }

    @PermitAll
    @PostMapping("/password-change-requests/{token}")
    public ResponseEntity<Void> passwordChange(@RequestBody PasswordChangeDto passwordChangeDto) throws ValidationException {
        passwordService.changePassword(passwordChangeDto);
        return ResponseEntity.ok().build();
    }


}
