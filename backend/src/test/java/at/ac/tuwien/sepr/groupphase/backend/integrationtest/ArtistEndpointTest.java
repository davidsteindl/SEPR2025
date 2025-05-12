package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ArtistEndpointTest implements TestData {

    private static final String ARTIST_BASE_URI = "/api/v1/artists/search";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    private Artist testArtist;

    @BeforeEach
    public void setup() {
        artistRepository.deleteAll();

        testArtist = new Artist();
        testArtist.setFirstname("John");
        testArtist.setLastname("Lennon");
        testArtist.setStagename("The Beatles");

        artistRepository.save(testArtist);
    }

    @Test
    void search_withMatchingStagename_returnsExpectedArtist() throws Exception {
        ArtistSearchDto search = new ArtistSearchDto();
        search.setStagename("beatles");

        MvcResult result = mockMvc.perform(post(ARTIST_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(search)))
            .andReturn();

        ArtistDto[] response = objectMapper.readValue(result.getResponse().getContentAsString(), ArtistDto[].class);

        assertAll(
            () -> assertEquals(1, response.length),
            () -> assertEquals("John", response[0].getFirstname()),
            () -> assertEquals("Lennon", response[0].getLastname()),
            () -> assertEquals("The Beatles", response[0].getStagename())
        );
    }

    @Test
    void search_withUnknownStagename_returnsEmptyList() throws Exception {
        ArtistSearchDto search = new ArtistSearchDto();
        search.setStagename("Mozart");

        MvcResult result = mockMvc.perform(post(ARTIST_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .content(objectMapper.writeValueAsString(search)))
            .andReturn();

        ArtistDto[] response = objectMapper.readValue(result.getResponse().getContentAsString(), ArtistDto[].class);

        assertAll(
            () -> assertNotNull(response),
            () -> assertEquals(0, response.length)
        );
    }
}
