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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
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


    @BeforeEach
    public void setup() {
        artistRepository.deleteAll();

        Artist artist = new Artist();
        artist.setFirstname("Freddie");
        artist.setLastname("Mercury");
        artist.setStagename("Queen");

        artistRepository.save(artist);
    }

    @Test
    public void searchArtists_withValidFirstname_returnsArtist() throws Exception {
        ArtistSearchDto searchDto = new ArtistSearchDto();
        searchDto.setFirstname("Freddie");
        searchDto.setPage(0);
        searchDto.setSize(10);

        String json = objectMapper.writeValueAsString(searchDto);

        MvcResult mvcResult = this.mockMvc.perform(post(ARTIST_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType()),
            () -> assertTrue(response.getContentAsString().contains("Freddie"))
        );
    }

    @Test
    public void searchArtists_withNoSearchCriteria_returns400() throws Exception {
        ArtistSearchDto searchDto = new ArtistSearchDto();
        searchDto.setPage(0);
        searchDto.setSize(10);

        String json = objectMapper.writeValueAsString(searchDto);

        MvcResult mvcResult = this.mockMvc.perform(post(ARTIST_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());
    }

    @Test
    public void searchArtists_withNonMatchingName_returnsEmptyResult() throws Exception {
        ArtistSearchDto searchDto = new ArtistSearchDto();
        searchDto.setFirstname("NonExistent");
        searchDto.setPage(0);
        searchDto.setSize(10);

        String json = objectMapper.writeValueAsString(searchDto);

        MvcResult mvcResult = this.mockMvc.perform(post(ARTIST_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertTrue(response.getContentAsString().contains("\"totalElements\":0"))
        );
    }
}
