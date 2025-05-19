package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.EventLocationEndpoint;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventLocationMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.EventLocationService;
import at.ac.tuwien.sepr.groupphase.backend.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventLocationEndpoint.class)
public class EventLocationEndpointSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private EventLocationService eventLocationService;

    @MockitoBean
    private EventLocationMapper eventLocationMapper;

    @MockitoBean
    private SecurityProperties securityProperties;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void searchNoDataReturnsEmptyContent() throws Exception {
        when(searchService.searchEventLocations(any(EventLocationSearchDto.class)))
            .thenReturn(new PageImpl<>(List.of()));

        String emptyDtoJson = "{\"page\":0,\"size\":10}";

        mockMvc.perform(post("/api/v1/locations/search")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(emptyDtoJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void searchWithDataReturnsContent() throws Exception {
        EventLocationDetailDto dto = EventLocationDetailDto.EventLocationDtoBuilder
            .anEventLocationDto()
            .id(5L)
            .name("Gasometer")
            .street("Guglgasse 6")
            .city("Vienna")
            .country("Austria")
            .postalCode("1110")
            .build();

        when(searchService.searchEventLocations(any(EventLocationSearchDto.class)))
            .thenReturn(new PageImpl<>(List.of(dto)));

        String dtoJson = """
            {
              "page": 0,
              "size": 1,
              "name": "Gasometer"
            }
        """;

        mockMvc.perform(post("/api/v1/locations/search")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(dtoJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(5))
            .andExpect(jsonPath("$.content[0].name").value("Gasometer"))
            .andExpect(jsonPath("$.content[0].street").value("Guglgasse 6"))
            .andExpect(jsonPath("$.content[0].city").value("Vienna"))
            .andExpect(jsonPath("$.content[0].country").value("Austria"))
            .andExpect(jsonPath("$.content[0].postalCode").value("1110"));
    }
}
