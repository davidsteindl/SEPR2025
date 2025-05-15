package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserEndpointTest implements TestData {

    private static final String USER_BASE_URI = "/api/v1/users";

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenizer jwtTokenizer;
    @Autowired private SecurityProperties securityProperties;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    private ApplicationUser testUser;


    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        testUser = new ApplicationUser();
        testUser.setEmail(DEFAULT_USER);
        testUser.setFirstName("Max");
        testUser.setLastName("Mustermann");
        testUser.setSex(Sex.OTHER);
        testUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testUser.setPassword("secret");
        userRepository.save(testUser);
    }

    @Test
    public void getCurrentUser_shouldReturnCorrectData() throws Exception {
        MvcResult result = mockMvc.perform(get(USER_BASE_URI + "/me")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andReturn();

        UserDetailDto dto = objectMapper.readValue(result.getResponse().getContentAsString(), UserDetailDto.class);

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(testUser.getEmail(), dto.getEmail());
    }

    @Test
    public void getCurrentUser_withoutAuth_shouldFail() throws Exception {
        MvcResult result = mockMvc.perform(get(USER_BASE_URI + "/me")).andReturn();
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void updateCurrentUser_shouldSucceed() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setFirstName("Erika");
        dto.setLastName("Musterfrau");
        dto.setEmail(DEFAULT_USER);
        dto.setDateOfBirth(LocalDate.of(1995, 5, 5));
        dto.setSex(Sex.OTHER);

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(put(USER_BASE_URI + "/me")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());
    }

    @Test
    public void updateCurrentUser_withInvalidData_shouldReturn422() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(put(USER_BASE_URI + "/me")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    public void deleteCurrentUser_shouldRemoveUser() throws Exception {
        MvcResult result = mockMvc.perform(delete(USER_BASE_URI + "/me")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());
        ApplicationUser deletedUser = userRepository.findByEmail(DEFAULT_USER);
        assertNull(deletedUser);

    }

    @Test
    public void deleteCurrentUser_withoutAuth_shouldFail() throws Exception {
        MvcResult result = mockMvc.perform(delete(USER_BASE_URI + "/me")).andReturn();
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }
}
