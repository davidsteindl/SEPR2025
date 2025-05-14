package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreateArtistDto;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ArtistEndpointTest implements TestData {

    private static final String ARTIST_BASE_URI = "/api/v1/artists";

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenizer jwtTokenizer;
    @Autowired private SecurityProperties securityProperties;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ArtistRepository artistRepository;

    @BeforeEach
    public void setup() {
        artistRepository.deleteAll();
    }

    @Test
    public void getAllArtists_shouldReturnEmptyInitially() throws Exception {
        MvcResult result = mockMvc.perform(get(ARTIST_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        List<ArtistDetailDto> artists = List.of(objectMapper.readValue(result.getResponse().getContentAsString(), ArtistDetailDto[].class));
        assertTrue(artists.isEmpty());
    }

    @Test
    public void createArtist_asAdmin_shouldSucceed() throws Exception {
        CreateArtistDto dto = CreateArtistDto.CreateArtistDtoBuilder.aCreateArtistDto()
            .firstname("John")
            .lastname("Doe")
            .stagename("JD")
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(ARTIST_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());

        ArtistDetailDto response = objectMapper.readValue(result.getResponse().getContentAsString(), ArtistDetailDto.class);
        assertAll(
            () -> assertEquals(dto.getFirstname(), response.getFirstname()),
            () -> assertEquals(dto.getLastname(), response.getLastname()),
            () -> assertEquals(dto.getStagename(), response.getStagename())
        );
    }

    @Test
    public void createArtist_asUser_shouldFailWith403() throws Exception {
        CreateArtistDto dto = CreateArtistDto.CreateArtistDtoBuilder.aCreateArtistDto()
            .firstname("Jane")
            .lastname("Smith")
            .stagename("JS")
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(ARTIST_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void createArtist_withoutAuth_shouldFailWith403() throws Exception {
        CreateArtistDto dto = CreateArtistDto.CreateArtistDtoBuilder.aCreateArtistDto()
            .firstname("Max")
            .lastname("Mustermann")
            .stagename("MM")
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(ARTIST_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void createArtist_withInvalidData_shouldReturn422() throws Exception {
        CreateArtistDto dto = new CreateArtistDto();
        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(ARTIST_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }
}
