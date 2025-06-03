package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetStandingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.AuthenticationFacade;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.when;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private String firstName;
    private String lastName;
    private String houseNumber;
    private String street;
    private String city;
    private String country;
    private String postalCode;

    private Show futureShow;
    private StandingSector sector;

    @MockitoBean
    private AuthenticationFacade authenticationFacade;

    @BeforeEach
    public void setup() {
        // stub current user id
        when(authenticationFacade.getCurrentUserId()).thenReturn(1L);

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
            .withDateTime(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES))
            .withDuration(180)
            .withLocation(location)
            .build();
        event = eventRepository.save(event);

        futureShow = Show.ShowBuilder.aShow()
            .withName("Test Show")
            .withDate(event.getDateTime().plusDays(1).truncatedTo(ChronoUnit.MINUTES))
            .withDuration(60).withRoom(room)
            .withEvent(event)
            .withArtists(Set.of())
            .build();
        futureShow = showRepository.save(futureShow);

        firstName = "Max";
        lastName = "Mustermann";
        street = "Main Street";
        houseNumber = "10";
        city = "Vienna";
        country = "Austria";
        postalCode = "1010";
    }

    @Test
    @Transactional
    public void reserveTickets_shouldCreateReservation_whenValidRequest() throws Exception {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(futureShow.getId());
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sector.getId());
        target.setQuantity(2);

        request.setTargets(List.of(target));


        String jwt = jwtTokenizer.getAuthToken("user@email.com", List.of("ROLE_USER"));

        MvcResult result = mockMvc.perform(post("/api/v1/tickets/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", jwt))
            .andReturn();
        System.out.println(result.getResponse().getContentAsString());


        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());

        assertAll(
            () -> assertNotNull(responseJson.get("id"), "Reservation ID should not be null"),
            () -> assertEquals("RESERVATION", responseJson.get("orderType").asText(), "Order type should be RESERVATION"),
            () -> assertEquals(2, responseJson.get("tickets").size(), "Should reserve 2 tickets")
        );
    }

    @Test
    @Transactional
    public void buyReservedTickets_shouldConvertReservationToOrder_whenValidRequest() throws Exception {
        TicketRequestDto reserveRequest = new TicketRequestDto();
        reserveRequest.setShowId(futureShow.getId());
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sector.getId());
        target.setQuantity(1);

        reserveRequest.setTargets(List.of(target));


        String jwt = jwtTokenizer.getAuthToken("user@email.com", List.of("ROLE_USER"));


        MvcResult reserveResult = mockMvc.perform(post("/api/v1/tickets/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveRequest))
                .header("Authorization", jwt))
            .andReturn();

        JsonNode reservation = objectMapper.readTree(reserveResult.getResponse().getContentAsString());
        Long reservationId = reservation.get("id").asLong();
        Long ticketId = reservation.get("tickets").get(0).get("id").asLong();

        MvcResult result = mockMvc.perform(post("/api/v1/tickets/reservations/" + reservationId + "/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(ticketId)))
                .header("Authorization", jwt))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        OrderDto response = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);
        assertAll(
            () -> assertNotNull(response.getId(), "Order ID should not be null"),
            () -> assertEquals(OrderType.ORDER, response.getOrderType(), "Order type should be ORDER"),
            () -> assertEquals(1, response.getTickets().size(), "One reserved ticket should be bought")
        );
    }

    @Test
    @Transactional
    public void cancelReservations_shouldReturnCancelledTickets_whenValidIds() throws Exception {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(futureShow.getId());
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sector.getId());
        target.setQuantity(2);
        request.setTargets(List.of(target));

        String jwt = jwtTokenizer.getAuthToken("user@email.com", List.of("ROLE_USER"));

        MvcResult reserveResult = mockMvc.perform(post("/api/v1/tickets/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", jwt))
            .andReturn();

        assertEquals(HttpStatus.CREATED.value(), reserveResult.getResponse().getStatus());

        JsonNode reserveJson = objectMapper.readTree(reserveResult.getResponse().getContentAsString());
        List<Long> ticketIds = reserveJson.get("tickets")
            .findValuesAsText("id")
            .stream().map(Long::parseLong).toList();

        MvcResult cancelResult = mockMvc.perform(post("/api/v1/tickets/cancel-reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticketIds))
                .header("Authorization", jwt))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), cancelResult.getResponse().getStatus());

        TicketDto[] cancelled = objectMapper.readValue(cancelResult.getResponse().getContentAsString(), TicketDto[].class);
        assertAll(
            () -> assertEquals(2, cancelled.length),
            () -> assertTrue(cancelled[0].getStatus().name().equals("CANCELLED")),
            () -> assertTrue(cancelled[1].getStatus().name().equals("CANCELLED"))
        );
    }


    @Test
    @Transactional
    public void cancelReservations_shouldThrow_whenInvalidTicketIdsGiven() throws Exception {
        List<Long> fakeIds = List.of(9999L, 8888L);
        String jwt = jwtTokenizer.getAuthToken("user@email.com", List.of("ROLE_USER"));

        Exception exception = assertThrows(Exception.class, () ->
            mockMvc.perform(post("/api/v1/tickets/cancel-reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(fakeIds))
                    .header("Authorization", jwt))
                .andReturn()
        );

        assertTrue(exception.getMessage().contains("Some requested tickets were not found"));
    }


    @Test
    @Transactional
    public void buyTickets_shouldFail_whenCreditCardInvalid() throws Exception {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(futureShow.getId());
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sector.getId());
        target.setQuantity(1);
        request.setTargets(List.of(target));
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setStreet(street);
        request.setHousenumber(houseNumber);
        request.setCity(city);
        request.setCountry(country);
        request.setPostalCode(postalCode);
        request.setCardNumber("INVALID");
        request.setSecurityCode("abc");
        request.setExpirationDate("13/99");

        String jwt = jwtTokenizer.getAuthToken("user@email.com", List.of("ROLE_USER"));

        MvcResult result = mockMvc.perform(post("/api/v1/tickets/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", jwt))
            .andReturn();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    @Transactional
    public void getOrderGroupsByCategory_shouldReturnOneGroup_whenValidReservationExists() throws Exception {
        TicketRequestDto reserveRequest = new TicketRequestDto();
        reserveRequest.setShowId(futureShow.getId());

        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sector.getId());
        target.setQuantity(1);
        reserveRequest.setTargets(List.of(target));

        String jwt = jwtTokenizer.getAuthToken("user@email.com", List.of("ROLE_USER"));

        mockMvc.perform(post("/api/v1/tickets/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveRequest))
                .header("Authorization", jwt))
            .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(
                get("/api/v1/tickets/order-groups")
                    .param("isReservation", "true")
                    .param("past", "false")
                    .header("Authorization", jwt)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());

        assertAll(
            () -> assertTrue(json.has("content")),
            () -> assertTrue(json.get("content").isArray()),
            () -> assertEquals(1, json.get("content").size(), "Expected one reservation group"),
            () -> assertTrue(json.get("content").get(0).has("orders")),
            () -> assertEquals(1, json.get("content").get(0).get("orders").size()),
            () -> assertEquals("Test Show", json.get("content").get(0).get("showName").asText())
        );
    }


    @Test
    @Transactional
    public void getOrderGroupsByCategory_shouldReturnEmptyPage_whenNoMatchingOrdersExist() throws Exception {

        String jwt = jwtTokenizer.getAuthToken("user@email.com", List.of("ROLE_USER"));

        MvcResult result = mockMvc.perform(
                get("/api/v1/tickets/order-groups")
                    .param("isReservation", "true")
                    .param("past", "false")
                    .header("Authorization", jwt)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());

        assertAll(
            () -> assertTrue(json.has("content")),
            () -> assertTrue(json.get("content").isArray()),
            () -> assertEquals(0, json.get("content").size(), "Expected empty list when no reservations exist")
        );
    }



}
