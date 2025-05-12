package at.ac.tuwien.sepr.groupphase.backend.integrationtest;


import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.SearchEventEndpoint;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.SearchService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchEventEndpoint.class)
public class SearchEventEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private ShowService showService;

    @MockitoBean
    private ShowMapper showMapper;

    @MockitoBean
    private SecurityProperties securityProperties;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void searchNoDataReturnsEmptyContent() throws Exception {
        when(searchService.searchEvents(any()))
            .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/events/search"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content").isEmpty());
    }
}
