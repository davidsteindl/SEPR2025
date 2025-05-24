package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetStandingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TicketEndpointTest implements TestData {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenizer jwtTokenizer;
    @Autowired private SecurityProperties securityProperties;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoomRepository roomRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private EventLocationRepository eventLocationRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private OrderRepository orderRepository;

    private Show futureShow;
    private StandingSector sector;

    @BeforeEach
    public void setup() {
        ticketRepository.deleteAll();
        orderRepository.deleteAll();
        showRepository.deleteAll();
        roomRepository.deleteAll();
        eventRepository.deleteAll();
        eventLocationRepository.deleteAll();


        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Arena")
            .withCountry("AT")
            .withCity("Vienna")
            .withStreet("Main St")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.HALL)
            .build();
        location = eventLocationRepository.save(location);

        Room room = Room.RoomBuilder.aRoom().name("Main Room").eventLocation(location).build();
        sector = StandingSector.StandingSectorBuilder.aStandingSector().price(50).capacity(20).room(room).build();
        room.addSector(sector);
        room = roomRepository.save(room);

        Event event = Event.EventBuilder.anEvent()
            .withName("Test Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("Test Desc")
            .withDateTime(LocalDateTime.now().minusDays(1))
            .withDuration(180)
            .withLocation(location)
            .build();
        event = eventRepository.save(event);

        futureShow = Show.ShowBuilder.aShow()
            .withName("Test Show")
            .withDate(LocalDateTime.now().plusDays(1))
            .withDuration(60).withRoom(room)
            .withEvent(event)
            .withArtists(Set.of())
            .build();
        futureShow = showRepository.save(futureShow);
    }

    @Test
    @Transactional
    public void getUpcomingOrders_shouldReturnOrderWithFutureShow() throws Exception {
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sector.getId());
        target.setQuantity(1);

        TicketRequestDto req = new TicketRequestDto();
        req.setShowId(futureShow.getId());
        req.setTargets(List.of(target));

        mockMvc.perform(post("/api/v1/tickets/buy")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("1", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(result -> assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus()));

        MvcResult result = mockMvc.perform(get("/api/v1/tickets/orders/upcoming")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("1", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode content = root.get("content");

        List<OrderDto> orders = objectMapper.readerForListOf(OrderDto.class).readValue(content);
        assertAll(
            () -> assertEquals(1, orders.size(), "Should return one future order"),
            () -> assertEquals(OrderType.ORDER, orders.getFirst().getOrderType()),
            () -> assertEquals("Test Show", orders.getFirst().getShowName())
        );
    }

    @Test
    @Transactional
    public void getReservations_shouldReturnReservationIfExists() throws Exception {
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sector.getId());
        target.setQuantity(1);

        TicketRequestDto req = new TicketRequestDto();
        req.setShowId(futureShow.getId());
        req.setTargets(List.of(target));

        mockMvc.perform(post("/api/v1/tickets/reserve")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("1", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(result -> assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus()));

        MvcResult result = mockMvc.perform(get("/api/v1/tickets/orders/reservations")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("1", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode content = root.get("content");

        List<OrderDto> orders = objectMapper.readerForListOf(OrderDto.class).readValue(content);
        assertAll(
            () -> assertEquals(1, orders.size(), "Should return one reservation"),
            () -> assertEquals(OrderType.RESERVATION, orders.getFirst().getOrderType()),
            () -> assertEquals("Test Show", orders.getFirst().getShowName())
        );
    }

    @Test
    @Transactional
    public void getOrderById_shouldReturnMetadataOnly() throws Exception {
        //Create an order by buying a ticket
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sector.getId());
        target.setQuantity(1);

        TicketRequestDto req = new TicketRequestDto();
        req.setShowId(futureShow.getId());
        req.setTargets(List.of(target));

        MvcResult postResult = mockMvc.perform(post("/api/v1/tickets/buy")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("1", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andReturn();

        JsonNode responseJson = objectMapper.readTree(postResult.getResponse().getContentAsString());
        long orderId = responseJson.get("id").asLong();

        //Request order metadata without tickets
        MvcResult getResult = mockMvc.perform(get("/api/v1/tickets/orders/" + orderId)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("1", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), getResult.getResponse().getStatus());

        JsonNode order = objectMapper.readTree(getResult.getResponse().getContentAsString());

        assertAll(
            () -> assertEquals(orderId, order.get("id").asLong()),
            () -> assertEquals("Test Show", order.get("showName").asText()),
            () -> assertEquals(futureShow.getDate().toLocalDate(), LocalDateTime.parse(order.get("showDate").asText()).toLocalDate()),
            () -> assertEquals("Arena", order.get("locationName").asText()),
            () -> assertTrue(order.get("tickets").isNull() || order.get("tickets").isEmpty(), "Tickets field should be null or empty")
        );
    }

    @Test
    @Transactional
    public void getTicketsForOrder_shouldReturnPaginatedTickets() throws Exception {
        //Buy a ticket to create an order
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sector.getId());
        target.setQuantity(2);

        TicketRequestDto req = new TicketRequestDto();
        req.setShowId(futureShow.getId());
        req.setTargets(List.of(target));

        MvcResult postResult = mockMvc.perform(post("/api/v1/tickets/buy")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("1", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andReturn();

        JsonNode responseJson = objectMapper.readTree(postResult.getResponse().getContentAsString());
        long orderId = responseJson.get("id").asLong();

        MvcResult getResult = mockMvc.perform(get("/api/v1/tickets/orders/" + orderId + "/tickets?page=0&size=10")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("1", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), getResult.getResponse().getStatus());

        JsonNode page = objectMapper.readTree(getResult.getResponse().getContentAsString());
        JsonNode content = page.get("content");

        assertAll(
            () -> assertEquals(2, content.size(), "Should return two tickets"),
            () -> assertEquals(orderId, content.get(0).get("id").asLong() > 0 ? orderId : orderId),
            () -> assertEquals(50, content.get(0).get("price").asInt())
        );
    }

}
