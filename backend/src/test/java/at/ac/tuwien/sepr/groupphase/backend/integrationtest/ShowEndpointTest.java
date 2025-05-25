package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.CreateShowDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event.EventCategory;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation.LocationType;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ShowEndpointTest implements TestData {

    private static final String SHOW_BASE_URI = "/api/v1/shows";

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenizer jwtTokenizer;
    @Autowired private SecurityProperties securityProperties;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ShowRepository showRepository;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private EventLocationRepository eventLocationRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private OrderRepository orderRepository;


    private Event testEvent;
    private Artist testArtist;
    private Room testRoom;

    @BeforeEach
    public void setup() {
        artistRepository.findAll().forEach(artist -> {
            artist.setShows(Set.of());
            artistRepository.save(artist);
        });

        ticketRepository.deleteAll();
        orderRepository.deleteAll();
        showRepository.deleteAll();
        artistRepository.deleteAll();
        eventRepository.deleteAll();
        roomRepository.deleteAll();
        eventLocationRepository.deleteAll();

        EventLocation location = new EventLocation();
        location.setName("Arena");
        location.setType(LocationType.HALL);
        location.setCountry("Austria");
        location.setCity("Vienna");
        location.setStreet("Main Street 1");
        location.setPostalCode("1010");
        eventLocationRepository.save(location);

        testRoom = Room.RoomBuilder.aRoom()
            .name("Main Room")
            .eventLocation(location)
            .build();
        roomRepository.save(testRoom);
        testRoom = roomRepository.save(testRoom);

        testEvent = new Event();
        testEvent.setName("Rock Night");
        testEvent.setCategory(EventCategory.ROCK);
        testEvent.setDescription("A night of rock music");
        testEvent.setDateTime(LocalDateTime.now().plusDays(5));
        testEvent.setDuration(180);
        testEvent.setLocation(location);
        eventRepository.save(testEvent);

        testArtist = new Artist();
        testArtist.setFirstname("John");
        testArtist.setLastname("Doe");
        testArtist.setStagename("JD");
        artistRepository.save(testArtist);
    }

    @Test
    public void createShow_asAdmin_shouldSucceed() throws Exception {
        CreateShowDto dto = CreateShowDto.CreateShowDtoBuilder.aCreateShowDto()
            .name("Opening Act")
            .duration(60)
            .date(testEvent.getDateTime().plusHours(1))
            .eventId(testEvent.getId())
            .roomId(testRoom.getId())
            .artistIds(Set.of(testArtist.getId()))
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOW_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());

        ShowDetailDto response = objectMapper.readValue(result.getResponse().getContentAsString(), ShowDetailDto.class);
        assertAll(
            () -> assertEquals(dto.getName(), response.getName()),
            () -> assertEquals(dto.getDuration(), response.getDuration()),
            () -> assertEquals(dto.getEventId(), response.getEventId()),
            () -> assertEquals(dto.getArtistIds(), response.getArtistIds())
        );
    }

    @Test
    public void getAllShows_shouldReturnEmptyInitially() throws Exception {
        MvcResult result = mockMvc.perform(get(SHOW_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        List<ShowDetailDto> shows = List.of(objectMapper.readValue(result.getResponse().getContentAsString(), ShowDetailDto[].class));
        assertTrue(shows.isEmpty());
    }

    @Test
    public void createShow_asUser_shouldFailWith403() throws Exception {
        CreateShowDto dto = CreateShowDto.CreateShowDtoBuilder.aCreateShowDto()
            .name("Unauthorized Show")
            .duration(90)
            .date(testEvent.getDateTime().plusHours(1))
            .eventId(testEvent.getId())
            .roomId(testRoom.getId())
            .artistIds(Set.of(testArtist.getId()))
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOW_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void createShow_withoutAuth_shouldFailWith403() throws Exception {
        CreateShowDto dto = CreateShowDto.CreateShowDtoBuilder.aCreateShowDto()
            .name("No Auth Show")
            .duration(90)
            .date(testEvent.getDateTime().plusHours(1))
            .eventId(testEvent.getId())
            .roomId(testRoom.getId())
            .artistIds(Set.of(testArtist.getId()))
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOW_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void createShow_withInvalidData_shouldReturn422() throws Exception {
        CreateShowDto dto = new CreateShowDto(); // Leeres DTO → ungültig
        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOW_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    public void createShow_withInvalidRoomId_shouldReturn422() throws Exception {
        CreateShowDto dto = CreateShowDto.CreateShowDtoBuilder.aCreateShowDto()
            .name("Invalid Room Show")
            .duration(90)
            .date(testEvent.getDateTime().plusHours(1))
            .eventId(testEvent.getId())
            .roomId(999L)
            .artistIds(Set.of(testArtist.getId()))
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOW_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    public void searchShows_asAdmin_shouldReturnMatchingShow() throws Exception {
        CreateShowDto createDto = CreateShowDto.CreateShowDtoBuilder.aCreateShowDto()
            .name("Rock Session")
            .duration(90)
            .date(testEvent.getDateTime().plusHours(1))
            .eventId(testEvent.getId())
            .roomId(testRoom.getId())
            .artistIds(Set.of(testArtist.getId()))
            .build();

        mockMvc.perform(post(SHOW_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(result -> assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus()));

        var searchDto = new Object() {
            public final int page = 0;
            public final int size = 10;
            public final String name = "Rock";
        };

        MvcResult result = mockMvc.perform(post(SHOW_BASE_URI + "/search")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchDto)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        String responseJson = result.getResponse().getContentAsString();
        assertTrue(responseJson.contains("Rock Session"));
    }

}
