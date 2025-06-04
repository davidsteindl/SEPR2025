package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.Sex;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthorizationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.PdfExportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfExportServiceImplTest {

    private TicketRepository ticketRepository;
    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private PdfExportServiceImpl pdfExportService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        orderRepository = mock(OrderRepository.class);
        userRepository = mock(UserRepository.class);

        pdfExportService = new PdfExportServiceImpl(ticketRepository, orderRepository, userRepository);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private Ticket createMockTicket(Long id, String code) {
        EventLocation location = mock(EventLocation.class);
        when(location.getStreet()).thenReturn("Musterstraße 12");
        when(location.getPostalCode()).thenReturn("12345");
        when(location.getCity()).thenReturn("Musterstadt");
        when(location.getName()).thenReturn("Musterhalle");

        Event event = mock(Event.class);
        when(event.getLocation()).thenReturn(location);
        when(event.getName()).thenReturn("Muster Event");

        Show show = mock(Show.class);
        when(show.getEvent()).thenReturn(event);
        when(show.getName()).thenReturn("Muster Show");
        when(show.getDate()).thenReturn(LocalDateTime.of(2025, 5, 31, 20, 0));

        Room room = mock(Room.class);
        when(room.getName()).thenReturn("Raum A");

        Sector sector = mock(Sector.class);
        when(sector.getRoom()).thenReturn(room);
        when(sector.getPrice()).thenReturn(30);

        Seat seat = mock(Seat.class);
        when(seat.getRowNumber()).thenReturn(5);
        when(seat.getColumnNumber()).thenReturn(12);

        Order order = mock(Order.class);
        when(order.getUserId()).thenReturn(1L);

        Ticket ticket = mock(Ticket.class);
        when(ticket.getId()).thenReturn(id);
        when(ticket.getRandomTicketCode()).thenReturn(code);
        when(ticket.getShow()).thenReturn(show);
        when(ticket.getSector()).thenReturn(sector);
        when(ticket.getSeat()).thenReturn(seat);
        when(ticket.getOrders()).thenReturn(List.of(order));
        when(order.getOrderType()).thenReturn(OrderType.ORDER);
        //when(ticket.getOrder()).thenReturn(order);

        return ticket;
    }



    @Test
    void makeTicketPdf_ThrowsNotFoundException_WhenTicketNotFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            pdfExportService.makeTicketPdf(1L, new ByteArrayOutputStream(), Optional.empty());
        });
    }

    @Test
    void makeTicketPdf_ThrowsAuthorizationException_WhenUserNotAuthorized() {
        Ticket ticketMock = createMockTicket(1L, "randomCode");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketMock));

        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .thenReturn("999");

        assertThrows(AuthorizationException.class, () -> {
            pdfExportService.makeTicketPdf(1L, new ByteArrayOutputStream(), Optional.empty());
        });
    }

    @Test
    void makeTicketPdf_ThrowsValidationException_WhenVerificationCodeInvalid() {
        Ticket ticketMock = createMockTicket(1L, "correctCode");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketMock));

        String wrongCode = "wrongCode";

        assertThrows(ValidationException.class, () -> {
            pdfExportService.makeTicketPdf(1L, new ByteArrayOutputStream(), Optional.of(wrongCode));
        });
    }

    @Test
    void makeTicketPdf_SuccessfullyGeneratesPdf_WithoutVerificationCode() {
        Ticket ticketMock = createMockTicket(1L, "randomCode");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketMock));

        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .thenReturn("1"); // korrekter User

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        assertDoesNotThrow(() -> {
            pdfExportService.makeTicketPdf(1L, outputStream, Optional.empty());
        });

        assertTrue(outputStream.size() > 0);
    }

    @Test
    void makeTicketPdf_SuccessfullyGeneratesPdf_WithVerificationCode() {
        Ticket ticketMock = createMockTicket(1L, "correctCode");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketMock));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        assertDoesNotThrow(() -> {
            pdfExportService.makeTicketPdf(1L, outputStream, Optional.of("correctCode"));
        });

        assertTrue(outputStream.size() > 0);
    }


    @Test
    void makeInvoicePdf_SuccessfulGeneratedPdf_ByAuthorizedUser() {
        Order order = mock(Order.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(order.getId()).thenReturn(1L);
        when(order.getUserId()).thenReturn(1L);

        Ticket ticketMock = createMockTicket(1L, "correctCode");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketMock));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(order.getOrderType()).thenReturn(OrderType.REFUND);
        when(order.getId()).thenReturn(1L);
        when(order.getUserId()).thenReturn(1L);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 5, 31, 10, 0));
        when(order.getTickets()).thenReturn(List.of(ticketMock));
        when(user.getSex()).thenReturn(Sex.OTHER);
        when(user.getFirstName()).thenReturn("Max");
        when(user.getLastName()).thenReturn("Mustermann");
        when(user.getStreet()).thenReturn("Musterstraße");
        when(user.getHousenumber()).thenReturn("12A");
        when(user.getPostalCode()).thenReturn("12345");
        when(user.getCity()).thenReturn("Musterstadt");
        when(user.getCountry()).thenReturn("Österreich");

        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn("1"); // Authorized user

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        assertDoesNotThrow(() -> {
            pdfExportService.makeInvoicePdf(1L, outputStream);
        });

        assertTrue(outputStream.size() > 0);
    }

    @Test
    void makeInvoicePdf_ThrowsAuthorizationException_WhenUserNotAuthorized() {
        Order order = mock(Order.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(order.getId()).thenReturn(1L);
        when(order.getUserId()).thenReturn(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn("999");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        assertThrows(AuthorizationException.class, () -> {
            pdfExportService.makeInvoicePdf(1L, outputStream);
        });
    }

    @Test
    void makeInvoicePdf_Successfully_WithCorrectOrderType() {
        Order order = mock(Order.class);
        ApplicationUser user = mock(ApplicationUser.class);
        Ticket ticketMock = createMockTicket(1L, "correctCode");

        when(order.getOrderType()).thenReturn(OrderType.REFUND);
        when(order.getId()).thenReturn(1L);
        when(order.getUserId()).thenReturn(1L);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 5, 31, 10, 0));
        when(order.getTickets()).thenReturn(List.of(ticketMock));
        when(user.getSex()).thenReturn(Sex.OTHER);
        when(user.getFirstName()).thenReturn("Max");
        when(user.getLastName()).thenReturn("Mustermann");
        when(user.getStreet()).thenReturn("Musterstraße");
        when(user.getHousenumber()).thenReturn("12A");
        when(user.getPostalCode()).thenReturn("12345");
        when(user.getCity()).thenReturn("Musterstadt");
        when(user.getCountry()).thenReturn("Österreich");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketMock));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn("1");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            pdfExportService.makeInvoicePdf(1L, outputStream);
        });

        assertTrue(outputStream.size() > 0, "PDF generation failed; output stream is empty");
    }

    @Test
    void makeCancelInvoicePdf_ThrowsAuthorizationException_WhenUserNotAuthorized() {

        Order order = mock(Order.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(order.getId()).thenReturn(1L);
        when(order.getUserId()).thenReturn(1L);
        when(order.getOrderType()).thenReturn(OrderType.CANCELLATION);
        when(user.getId()).thenReturn(1L);

        Ticket ticketMock = createMockTicket(1L, "correctCode");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketMock));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn("999");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        assertThrows(AuthorizationException.class, () -> {
            pdfExportService.makeCancelInvoicePdf(1L, outputStream);
        });

    }

}
