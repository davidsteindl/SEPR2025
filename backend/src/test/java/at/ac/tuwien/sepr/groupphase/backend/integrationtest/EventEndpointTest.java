package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.CreateEventDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.UpdateEventDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event.EventCategory;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation.LocationType;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
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
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class EventEndpointTest implements TestData {

    private static final String EVENT_BASE_URI = "/api/v1/events";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenizer jwtTokenizer;
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventLocationRepository eventLocationRepository;
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ArtistRepository artistRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private OrderRepository orderRepository;


    private EventLocation testLocation;
    private Event testEvent;
    private Artist testArtist;
    private Room testRoom;
    private Show testShow;

    @BeforeEach
    public void setup() {
        ticketRepository.deleteAll();
        orderRepository.deleteAll();
        artistRepository.deleteAll();
        showRepository.deleteAll();
        eventRepository.deleteAll();
        roomRepository.deleteAll();
        eventLocationRepository.deleteAll();

        testLocation = new EventLocation();
        testLocation.setName("Testhalle");
        testLocation.setType(LocationType.HALL);
        testLocation.setCountry("Austria");
        testLocation.setCity("Vienna");
        testLocation.setStreet("Teststraße 1");
        testLocation.setPostalCode("1010");
        eventLocationRepository.save(testLocation);

        testEvent = new Event();
        testEvent.setName("Jazzkonzert");
        testEvent.setCategory(EventCategory.JAZZ);
        testEvent.setDescription("Jazz für alle");
        testEvent.setDuration(120);
        testEvent.setDateTime(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES));
        testEvent.setLocation(testLocation);
        eventRepository.save(testEvent);

        testArtist = new Artist();
        testArtist.setFirstname("Lena");
        testArtist.setLastname("Funk");
        testArtist.setStagename("LF");
        artistRepository.save(testArtist);

        testRoom = Room.RoomBuilder.aRoom()
            .withName("Test Room A")
            .withEventLocation(testLocation)
            .build();
        roomRepository.save(testRoom);

        testShow = Show.ShowBuilder.aShow()
            .withName("Funky Evening")
            .withDuration(75)
            .withDate(java.time.LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withRoom(testRoom)
            .build();
        testShow.addArtist(testArtist);
        showRepository.save(testShow);
    }

    @Test
    public void getEventById_shouldReturnCorrectData() throws Exception {
        MvcResult result = mockMvc.perform(get(EVENT_BASE_URI + "/" + testEvent.getId())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        EventDetailDto dto = objectMapper.readValue(result.getResponse().getContentAsString(), EventDetailDto.class);
        assertAll(
            () -> assertEquals(testEvent.getName(), dto.getName()),
            () -> assertEquals(testEvent.getCategory().name(), dto.getCategory())
        );
    }

    @Test
    public void getAllEvents_shouldReturnList() throws Exception {
        MvcResult result = mockMvc.perform(get(EVENT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        List<EventDetailDto> events = List.of(objectMapper.readValue(result.getResponse().getContentAsString(), EventDetailDto[].class));
        assertFalse(events.isEmpty());
    }

    @Test
    public void createEvent_asAdmin_shouldSucceed() throws Exception {
        CreateEventDto dto = CreateEventDto.CreateEventDtoBuilder.aCreateEventDto()
            .name("Rocknacht")
            .category("ROCK")
            .description("Laut und wild")
            .dateTime(LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MINUTES))
            .duration(180)
            .locationId(testLocation.getId())
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(EVENT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());

        EventDetailDto created = objectMapper.readValue(result.getResponse().getContentAsString(), EventDetailDto.class);
        assertAll(
            () -> assertEquals(dto.getName(), created.getName()),
            () -> assertEquals(dto.getCategory(), created.getCategory())
        );
    }

    @Test
    public void createEvent_asUser_shouldFailWith403() throws Exception {
        CreateEventDto dto = CreateEventDto.CreateEventDtoBuilder.aCreateEventDto()
            .name("UserEvent")
            .category("POP")
            .description("Normaler User darf nicht")
            .dateTime(LocalDateTime.now().plusDays(4).truncatedTo(ChronoUnit.MINUTES))
            .duration(90)
            .locationId(testLocation.getId())
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(EVENT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void createEvent_withoutAuth_shouldFail() throws Exception {
        CreateEventDto dto = CreateEventDto.CreateEventDtoBuilder.aCreateEventDto()
            .name("Anonymous")
            .category("CLASSICAL")
            .description("Kein Token vorhanden")
            .dateTime(LocalDateTime.now().plusDays(5).truncatedTo(ChronoUnit.MINUTES))
            .duration(60)
            .locationId(testLocation.getId())
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(EVENT_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void createEvent_withInvalidData_shouldReturn422() throws Exception {
        CreateEventDto dto = new CreateEventDto();
        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(EVENT_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    public void getEventsByArtist_shouldReturnLinkedEvents() throws Exception {
        var artist = new at.ac.tuwien.sepr.groupphase.backend.entity.Artist();
        artist.setFirstname("Max");
        artist.setLastname("Muster");
        artist.setStagename("MaxStar");

        var show = at.ac.tuwien.sepr.groupphase.backend.entity.Show.ShowBuilder.aShow()
            .withName("Evening Show")
            .withDuration(90)
            .withDate(java.time.LocalDateTime.now().plusDays(1))
            .withRoom(testRoom)
            .withEvent(testEvent)
            .build();

        show.addArtist(artist);

        showRepository.save(show);
        artistRepository.save(artist);

        MvcResult result = mockMvc.perform(get(EVENT_BASE_URI + "/by-artist/" + artist.getId())
                .param("page", "0")
                .param("size", "5")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("Jazzkonzert"));
    }


    @Test
    public void getPaginatedShowsForEvent_shouldReturnPaginatedShows() throws Exception {
        MvcResult result = mockMvc.perform(get(EVENT_BASE_URI + "/" + testEvent.getId() + "/shows/paginated")
                .param("page", "0")
                .param("size", "5")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        String body = result.getResponse().getContentAsString();

        assertAll(
            () -> assertTrue(body.contains("Funky Evening"), "Show name should be present"),
            () -> assertTrue(body.contains("totalElements"), "Pagination info should be included"),
            () -> assertTrue(body.contains("\"content\":"), "Response should contain 'content' field")
        );
    }

    @Test
    public void getAllEventCategories_shouldReturnAllCategories() throws Exception {
        MvcResult result = mockMvc.perform(get(EVENT_BASE_URI + "/categories")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        String responseBody = result.getResponse().getContentAsString();
        assertAll(
            () -> assertTrue(responseBody.contains("Jazz"), "Category 'Jazz' should be in the result"),
            () -> assertTrue(responseBody.contains("Rock"), "Category 'Rock' should be in the result"),
            () -> assertTrue(responseBody.contains("displayName"), "Each category should have a displayName field")
        );
    }

    @Test
    public void updateEvent_withValidData_shouldSucceed() throws Exception {
        showRepository.deleteAll();

        UpdateEventDto dto = UpdateEventDto.UpdateEventDtoBuilder.anUpdateEventDto()
            .name("Updated Concert")
            .category(testEvent.getCategory().name())
            .description("Updated description")
            .dateTime(testEvent.getDateTime().plusDays(1))
            .duration(90)
            .locationId(testLocation.getId())
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(put(EVENT_BASE_URI + "/" + testEvent.getId())
                .header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        UpdateEventDto updated = objectMapper.readValue(
            result.getResponse().getContentAsString(), UpdateEventDto.class);

        assertAll(
            () -> assertEquals(dto.getName(), updated.getName()),
            () -> assertEquals(dto.getDescription(), updated.getDescription()),
            () -> assertEquals(dto.getDuration(), updated.getDuration()),
            () -> assertEquals(dto.getDateTime(), updated.getDateTime()),
            () -> assertEquals(dto.getLocationId(), updated.getLocationId())
        );
    }

    @Test
    public void updateEvent_withInvalidData_shouldReturn422() throws Exception {
        UpdateEventDto dto = UpdateEventDto.UpdateEventDtoBuilder.anUpdateEventDto()
            .name("   ")
            .category(testEvent.getCategory().name())
            .description("Desc")
            .dateTime(testEvent.getDateTime())
            .duration(60)
            .locationId(testLocation.getId())
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(put(EVENT_BASE_URI + "/" + testEvent.getId())
                .header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(),
            result.getResponse().getStatus());

        String json = result.getResponse().getContentAsString();
        assertTrue(json.contains("Name must not be blank"));
    }

    @Test
    public void updateEvent_asAdmin_shouldSucceed() throws Exception {
        showRepository.deleteAll();

        UpdateEventDto dto = UpdateEventDto.UpdateEventDtoBuilder.anUpdateEventDto()
            .name("JazzNacht")
            .category(testEvent.getCategory().name())
            .description("Mit Groove")
            .dateTime(testEvent.getDateTime().plusHours(2))
            .duration(testEvent.getDuration() + 30)
            .locationId(testLocation.getId())
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(put(EVENT_BASE_URI + "/" + testEvent.getId())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        UpdateEventDto updated = objectMapper.readValue(result.getResponse().getContentAsString(), UpdateEventDto.class);
        assertAll(
            () -> assertEquals(dto.getName(),        updated.getName()),
            () -> assertEquals(dto.getDescription(), updated.getDescription()),
            () -> assertEquals(dto.getDuration(),    updated.getDuration()),
            () -> assertEquals(dto.getDateTime(),    updated.getDateTime()),
            () -> assertEquals(dto.getLocationId(),  updated.getLocationId())
        );
    }

    @Test
    public void updateEvent_asUser_shouldFailWith403() throws Exception {
        UpdateEventDto dto = UpdateEventDto.UpdateEventDtoBuilder.anUpdateEventDto()
            .name("UserUpdate")
            .category("POP")
            .description("Normaler User darf nicht")
            .dateTime(testEvent.getDateTime())
            .duration(testEvent.getDuration())
            .locationId(testLocation.getId())
            .build();

        String body = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(put(EVENT_BASE_URI + "/" + testEvent.getId())
                .header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }


}
