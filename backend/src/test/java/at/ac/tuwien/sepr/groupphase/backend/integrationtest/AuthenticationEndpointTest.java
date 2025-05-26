package at.ac.tuwien.sepr.groupphase.backend.integrationtest;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.AuthenticationEndpoint;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
public class AuthenticationEndpointTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationEndpoint authenticationEndpoint;

    private UserRegisterDto testUserRegisterDto;

    @BeforeEach
    void setUp() {
        testUserRegisterDto = UserRegisterDto.UserRegisterDtoBuilder.anUserRegisterDto()
            .withFirstName("FirstName")
            .withLastName("LastName")
            .withDateOfBirth(LocalDate.of(1990, 1, 2))
            .withEmail("user@email.com")
            .withPassword("encodedPassword")
            .withConfirmPassword("encodedPassword")
            .withTermsAccepted(true)
            .build();
    }

    @Test
    void registerUser() throws ValidationException {

        assert(Objects.equals(authenticationEndpoint.register(testUserRegisterDto), ResponseEntity.status(HttpStatus.CREATED).build()));

    }



}
