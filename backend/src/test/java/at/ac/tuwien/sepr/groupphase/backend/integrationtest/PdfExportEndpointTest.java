package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static at.ac.tuwien.sepr.groupphase.backend.config.type.Sex.FEMALE;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PdfExportEndpointTest implements TestData {

    private static final String PDF_BASE_URI = "/api/v1/pdf-export";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private Order refundOrder;

    private ApplicationUser user;

    private Ticket testTicket;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventLocationRepository eventLocationRepository;
    @Autowired
    private SectorRepository sectorRepository;
    private Long testOrderId;

    @Transactional
    @BeforeEach
    public void setup() {
        ticketRepository.deleteAll();
        orderRepository.deleteAll();
        artistRepository.deleteAll();
        showRepository.deleteAll();
        eventRepository.deleteAll();
        roomRepository.deleteAll();
        sectorRepository.deleteAll();
        eventLocationRepository.deleteAll();
        userRepository.deleteAll();

        user = new ApplicationUser();
        user.setFirstName("Margit");
        user.setLastName("Tanne");
        user.setEmail("margit@gmail.com");
        user.setSex(FEMALE);
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setPostalCode("1545");
        user.setHousenumber("4545");
        user.setStreet("Strasse");
        user.setPassword("secret");
        user.setCity("Berlin");
        user.setCountry("Germany");
        userRepository.save(user);
        System.out.println(user);

        EventLocation testLocation = new EventLocation();
        testLocation.setName("Testhalle");
        testLocation.setType(EventLocation.LocationType.HALL);
        testLocation.setCountry("Austria");
        testLocation.setCity("Vienna");
        testLocation.setStreet("Teststraße 1");
        testLocation.setPostalCode("1010");
        eventLocationRepository.save(testLocation);

        Event testEvent = new Event();
        testEvent.setName("Jazzkonzert");
        testEvent.setCategory(Event.EventCategory.JAZZ);
        testEvent.setDescription("Jazz für alle");
        testEvent.setDuration(120);
        testEvent.setDateTime(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES));
        testEvent.setLocation(testLocation);
        eventRepository.save(testEvent);

        Artist testArtist = new Artist();
        testArtist.setFirstname("Lena");
        testArtist.setLastname("Funk");
        testArtist.setStagename("LF");
        artistRepository.save(testArtist);

        Room testRoom = Room.RoomBuilder.aRoom()
            .name("Test Room A")
            .eventLocation(testLocation)
            .build();
        roomRepository.save(testRoom);

        Show testShow = Show.ShowBuilder.aShow()
            .withName("Funky Evening")
            .withDuration(75)
            .withDate(java.time.LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withRoom(testRoom)
            .build();
        testShow.addArtist(testArtist);
        showRepository.save(testShow);

        Order order = new Order();
        order.setUserId(user.getId());
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderType(OrderType.ORDER);
        orderRepository.save(order);
        testOrderId = order.getId();

        refundOrder = new Order();
        refundOrder.setUserId(user.getId());
        refundOrder.setCreatedAt(LocalDateTime.now());
        refundOrder.setOrderType(OrderType.REFUND);
        orderRepository.save(refundOrder);

        Sector sector = new StandingSector();
        sector.setPrice(30);
        sector.setRoom(testRoom);
        sectorRepository.save(sector);

        testTicket = new Ticket();
        testTicket.setShow(testShow);
        testTicket.setSector(sector);
        order.setTickets(List.of(testTicket));
        testTicket.setOrders(List.of(order));
        //testTicket.setOrder(order);
        testTicket.setCreatedAt(LocalDateTime.now());
        testTicket.setStatus(TicketStatus.BOUGHT);
        testTicket.setRandomTicketCode("4d5d4d7ddddd44");
        ticketRepository.save(testTicket);

    }

    @Test
    public void exportTicketPdf_shouldSucceed() throws Exception {
        MvcResult result = mockMvc.perform(get(PDF_BASE_URI + "/tickets/" + testTicket.getId())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(user.getId().toString(), List.of("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn();

        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    public void exportTicketPdf_shouldFail() throws Exception {
        MvcResult result = mockMvc.perform(get(PDF_BASE_URI + "/tickets/" + testTicket.getId()))
            .andExpect(status().isForbidden())
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void viewTicketPdf_shouldSucceed() throws Exception {
        String randomCode = "4d5d4d7ddddd44";

        MvcResult result = mockMvc.perform(get(PDF_BASE_URI + "/tickets/" + testTicket.getId() + "/" + randomCode)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(user.getId().toString(), List.of("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn();

        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
        assertNotNull(pdfBytes);

    }


    @Test
    public void viewTicketPdf_shouldFail() throws Exception {
        MvcResult result = mockMvc.perform(get(PDF_BASE_URI + "/tickets/" + testTicket.getId() + "/invalidCode")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(user.getId().toString(), List.of("ROLE_ADMIN"))))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    public void exportInvoicePdf_shouldSucceed() throws Exception {
        MvcResult result = mockMvc.perform(get(PDF_BASE_URI + "/invoice/" + testOrderId)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(user.getId().toString(), List.of("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn();
        /*
        MvcResult result = mockMvc.perform(get(PDF_BASE_URI + "/invoice/" + testTicket.getOrder().getId())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(user.getId().toString(), List.of("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn();
        */

        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }



    @Test
    public void exportInvoicePdf_shouldFailWithoutAuthorization() throws Exception {
        MvcResult result = mockMvc.perform(get(PDF_BASE_URI + "/invoice/" + testOrderId))
            .andExpect(status().isForbidden())
            .andReturn();

        /*
        MvcResult result = mockMvc.perform(get(PDF_BASE_URI + "/invoice/" + testTicket.getOrder().getId()))
        .andExpect(status().isForbidden())
        .andReturn();

         */

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void exportCancelInvoicePdf_shouldSucceed() throws Exception {
        MvcResult result = mockMvc.perform(get(PDF_BASE_URI + "/cancelinvoice/" + refundOrder.getId())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(user.getId().toString(), List.of("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andReturn();

        byte[] pdfBytes = result.getResponse().getContentAsByteArray();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }



    @Test
    public void exportCancelInvoicePdf_shouldFailWithoutAuthorization() throws Exception {
        MvcResult result = mockMvc.perform(get(PDF_BASE_URI + "/cancelinvoice/" + refundOrder.getId()))
            .andExpect(status().isForbidden())
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }



}
