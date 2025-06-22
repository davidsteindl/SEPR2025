package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetSeatedDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetStandingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Hold;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.SeatUnavailableException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HoldRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.AuthenticationFacade;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.RoomServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ShowServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.TicketValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class TicketValidatorTest {

    private TicketValidator validator;

    private ShowServiceImpl showService;
    private RoomServiceImpl roomService;
    private HoldRepository holdRepository;
    private TicketRepository ticketRepository;
    @Mock
    private AuthenticationFacade authFacade;


    private Show show;
    private Sector sector;
    private StandingSector standingSector;
    private Seat seat;

    @BeforeEach
    void setUp() {
        showService = mock(ShowServiceImpl.class);
        roomService = mock(RoomServiceImpl.class);
        holdRepository = mock(HoldRepository.class);
        ticketRepository = mock(TicketRepository.class);
        authFacade = mock(AuthenticationFacade.class);

        validator = new TicketValidator(showService, roomService, holdRepository, ticketRepository, authFacade);


        show = new Show();
        show.setId(1L);
        show.setDate(LocalDateTime.now().plusDays(1));

        sector = new Sector();
        sector.setId(10L);

        standingSector = new StandingSector();
        standingSector.setId(20L);
        standingSector.setCapacity(10);

        seat = new Seat();
        seat.setId(100L);
        seat.setSector(sector);

        var room = new at.ac.tuwien.sepr.groupphase.backend.entity.Room();
        room.setSectors(Set.of(sector, standingSector));
        show.setRoom(room);

        when(showService.getShowById(any())).thenReturn(show);
        when(roomService.getSectorById(10L)).thenReturn(sector);
        when(roomService.getSectorById(20L)).thenReturn(standingSector);
        when(roomService.getSeatById(100L)).thenReturn(seat);
        when(authFacade.getCurrentUserId()).thenReturn(1L);
    }

    @Test
    public void validateForBuyTickets_shouldPassForValidSeated() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setShowId(show.getId());
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        dto.setTargets(List.of(target));

        when(ticketRepository.findByShowId(show.getId())).thenReturn(List.of());

        assertDoesNotThrow(() -> validator.validateForBuyTickets(dto));
    }

    @Test
    public void validateForBuyTickets_shouldThrowIfShowHasStarted() {
        show.setDate(LocalDateTime.now().minusMinutes(1));

        TicketRequestDto dto = new TicketRequestDto();
        dto.setShowId(show.getId());
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        dto.setTargets(List.of(target));

        assertThrows(SeatUnavailableException.class, () -> validator.validateForBuyTickets(dto));
    }

    @Test
    public void validateCheckoutPaymentData_shouldDetectInvalidCard() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setCardNumber("1234");
        dto.setExpirationDate("13/99");
        dto.setSecurityCode("12");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutPaymentData(dto));

        assertTrue(ex.getMessage().toLowerCase().contains("credit card"));
    }

    @Test
    public void validateCheckoutPaymentData_shouldPassForValidCard() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setCardNumber("4242424242424242");
        dto.setExpirationDate("12/30");
        dto.setSecurityCode("123");

        assertDoesNotThrow(() -> validator.validateCheckoutPaymentData(dto));
    }

    @Test
    public void validateCheckoutAddress_shouldThrowForIncomplete() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("");
        dto.setLastName(null);
        dto.setStreet("Main St");
        dto.setHousenumber("");
        dto.setPostalCode("1010");
        dto.setCity("Vienna");
        dto.setCountry(null);

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("First name")),
            () -> assertTrue(ex.getMessage().contains("Last name")),
            () -> assertTrue(ex.getMessage().toLowerCase().contains("incomplete"))
        );
    }

    @Test
    public void validateCheckoutAddress_shouldPassForComplete() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setStreet("Main Street");
        dto.setHousenumber("10");
        dto.setPostalCode("1010");
        dto.setCity("Vienna");
        dto.setCountry("Austria");

        assertDoesNotThrow(() -> validator.validateCheckoutAddress(dto));
    }

    @Test
    public void validateTargetsBelongToShow_shouldThrowIfStandingCapacityExceeded() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setShowId(show.getId());
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(standingSector.getId());
        target.setQuantity(15); // mehr als capacity
        dto.setTargets(List.of(target));

        when(ticketRepository.findByShowId(show.getId())).thenReturn(List.of());

        assertThrows(SeatUnavailableException.class, () -> validator.validateForBuyTickets(dto));
    }

    @Test
    public void validateNoTicketsOn_shouldThrowIfSeatAlreadyTaken() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setShowId(show.getId());
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        dto.setTargets(List.of(target));

        Ticket existing = new Ticket();
        existing.setShow(show);
        existing.setSeat(seat);
        existing.setStatus(TicketStatus.BOUGHT);

        when(ticketRepository.findByShowId(show.getId())).thenReturn(List.of(existing));

        assertThrows(SeatUnavailableException.class, () -> validator.validateForBuyTickets(dto));
    }

    @Test
    public void validateCheckoutAddress_shouldFailWhenFirstNameTooLong() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("A".repeat(101));
        dto.setLastName("Doe");
        dto.setStreet("Main Street");
        dto.setHousenumber("10");
        dto.setPostalCode("1010");
        dto.setCity("Vienna");
        dto.setCountry("Austria");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("First name")),
            () -> assertTrue(ex.getMessage().contains("too long"))
        );
    }

    @Test
    public void validateCheckoutAddress_shouldFailWhenLastNameTooLong() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("John");
        dto.setLastName("B".repeat(101));
        dto.setStreet("Main Street");
        dto.setHousenumber("10");
        dto.setPostalCode("1010");
        dto.setCity("Vienna");
        dto.setCountry("Austria");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("Last name")),
            () -> assertTrue(ex.getMessage().contains("too long"))
        );
    }

    @Test
    public void validateCheckoutAddress_shouldFailWhenStreetTooLong() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setStreet("C".repeat(201));
        dto.setHousenumber("10");
        dto.setPostalCode("1010");
        dto.setCity("Vienna");
        dto.setCountry("Austria");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("Street")),
            () -> assertTrue(ex.getMessage().contains("too long"))
        );
    }

    @Test
    public void validateCheckoutAddress_shouldFailWhenHousenumberTooLong() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setStreet("Main Street");
        dto.setHousenumber("D".repeat(101));
        dto.setPostalCode("1010");
        dto.setCity("Vienna");
        dto.setCountry("Austria");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("House number")),
            () -> assertTrue(ex.getMessage().contains("too long"))
        );
    }

    @Test
    public void validateCheckoutAddress_shouldFailWhenPostalCodeTooLong() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setStreet("Main Street");
        dto.setHousenumber("10");
        dto.setPostalCode("1".repeat(21));
        dto.setCity("Vienna");
        dto.setCountry("Austria");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("Postal code")),
            () -> assertTrue(ex.getMessage().contains("too long"))
        );
    }

    @Test
    public void validateCheckoutAddress_shouldFailWhenCityTooLong() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setStreet("Main Street");
        dto.setHousenumber("10");
        dto.setPostalCode("1010");
        dto.setCity("E".repeat(101));
        dto.setCountry("Austria");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("City")),
            () -> assertTrue(ex.getMessage().contains("too long"))
        );
    }

    @Test
    public void validateCheckoutAddress_shouldFailWhenCountryTooLong() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setStreet("Main Street");
        dto.setHousenumber("10");
        dto.setPostalCode("1010");
        dto.setCity("Vienna");
        dto.setCountry("F".repeat(101));

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("Country")),
            () -> assertTrue(ex.getMessage().contains("too long"))
        );
    }

    @Test
    public void validateCheckoutAddress_shouldFailWhenAddressIncomplete() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setStreet("Main Street");
        dto.setHousenumber(null);
        dto.setPostalCode("1010");
        dto.setCity("Vienna");
        dto.setCountry(null);

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().toLowerCase().contains("incomplete"))
        );
    }

    @Test
    public void validateCheckoutPaymentData_shouldAcceptValidLuhnNumber() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setCardNumber("4242424242424242");
        dto.setExpirationDate("12/30");
        dto.setSecurityCode("123");

        assertDoesNotThrow(() -> validator.validateCheckoutPaymentData(dto));
    }

    @Test
    public void validateCheckoutPaymentData_shouldRejectInvalidLuhnNumber() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setCardNumber("4242424242424243");
        dto.setExpirationDate("12/30");
        dto.setSecurityCode("123");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutPaymentData(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().toLowerCase().contains("luhn"))
        );
    }

    @Test
    public void validateCheckoutPaymentData_shouldRejectInvalidCardNumberFormat() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setCardNumber("abc123");
        dto.setExpirationDate("12/30");
        dto.setSecurityCode("123");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutPaymentData(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("Invalid credit card number format"))
        );
    }

    @Test
    public void validateCheckoutPaymentData_shouldRejectInvalidExpirationDateFormat() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setCardNumber("4242424242424242");
        dto.setExpirationDate("2024-12");
        dto.setSecurityCode("123");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutPaymentData(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("Invalid expiration date format"))
        );
    }

    @Test
    public void validateCheckoutPaymentData_shouldRejectUnparseableExpirationDate() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setCardNumber("4242424242424242");
        dto.setExpirationDate("siodoa");
        dto.setSecurityCode("123");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutPaymentData(dto));

        assertTrue(ex.getMessage().contains("Invalid expiration date format"));
    }

    @Test
    public void validateCheckoutPaymentData_shouldRejectInvalidCVC() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setCardNumber("4242424242424242");
        dto.setExpirationDate("12/30");
        dto.setSecurityCode("12");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutPaymentData(dto));

        assertAll(
            () -> assertTrue(ex.getMessage().contains("CVC"))
        );
    }

    @Test
    public void validateTicketsOwnedByCurrentUser_shouldThrowWhenTicketOwnedByOtherUser() {
        Ticket ticket = new Ticket();
        Show show = new Show();
        show.setDate(LocalDateTime.now().plusDays(1));
        ticket.setShow(show);
        ticket.setStatus(TicketStatus.RESERVED);
        Order order = new Order();
        order.setUserId(999L);
        ticket.setOrders(List.of(order));

        when(authFacade.getCurrentUserId()).thenReturn(1L);

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateForCancelReservations(List.of(1L), List.of(ticket)));

        assertTrue(ex.getMessage().contains("Cannot operate on tickets you do not own"));
    }

    @Test
    public void validateRefundWindow_shouldThrowIfShowAlreadyStarted() {
        Ticket ticket = new Ticket();
        ticket.setId(42L);
        ticket.setStatus(TicketStatus.BOUGHT);

        Show show = new Show();
        show.setDate(LocalDateTime.now().minusMinutes(1));
        ticket.setShow(show);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> validator.validateForRefundTickets(List.of(1L), List.of(ticket))
        );

        assertTrue(ex.getMessage().contains("Too late to refund"));
    }

    @Test
    public void validateForBuyTickets_shouldThrowIfShowAlreadyStarted() {
        Show show = new Show();
        show.setId(1L);
        show.setDate(LocalDateTime.now().minusMinutes(1));

        Room room = new Room();
        room.setId(1L);
        Sector sector = new Sector();
        sector.setId(1L);
        room.setSectors(Set.of(sector));
        show.setRoom(room);

        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(1L);
        target.setSeatId(1L);

        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(1L);
        request.setTargets(List.of(target));

        when(showService.getShowById(1L)).thenReturn(show);

        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSector(sector);

        when(roomService.getSectorById(1L)).thenReturn(sector);
        when(roomService.getSeatById(1L)).thenReturn(seat);
        when(ticketRepository.findByShowId(1L)).thenReturn(List.of());

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateForBuyTickets(request)
        );

        assertTrue(ex.getMessage().contains("Cannot buy tickets after show has started"));
    }



    @Test
    public void validateBeforeShowStarts_shouldThrowIfWithin30Minutes() {
        Show show = new Show();
        show.setDate(LocalDateTime.now().plusMinutes(29));
        show.setId(1L);

        when(showService.getShowById(1L)).thenReturn(show);

        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(1L);
        request.setTargets(List.of());

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateForReserveTickets(request)
        );

        assertTrue(ex.getMessage().contains("Cannot reserve tickets less than"));
    }


    @Test
    public void validateForBuyTickets_shouldThrowIfStandingQuantityZero() {
        Long showId = 1L;
        Long sectorId = 1L;

        // Show setup
        Show show = new Show();
        show.setId(showId);
        Room room = new Room();
        StandingSector sector = new StandingSector();
        sector.setId(sectorId);
        sector.setCapacity(100);
        room.setSectors(Set.of(sector));
        show.setRoom(room);
        show.setDate(LocalDateTime.now().plusHours(1));
        when(showService.getShowById(showId)).thenReturn(show);
        when(roomService.getSectorById(sectorId)).thenReturn(sector);
        when(ticketRepository.findByShowId(showId)).thenReturn(List.of());

        // Target with quantity 0
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sectorId);
        target.setQuantity(0);

        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(showId);
        request.setTargets(List.of(target));

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateForBuyTickets(request)
        );

        assertTrue(ex.getMessage().contains("quantity must be greater than zero"));
    }

    @Test
    public void validateForBuyTickets_shouldThrowIfStandingOverbooked() {
        Long showId = 1L;
        Long sectorId = 1L;

        StandingSector sector = new StandingSector();
        sector.setId(sectorId);
        sector.setCapacity(5);

        Show show = new Show();
        show.setId(showId);
        Room room = new Room();
        room.setSectors(Set.of(sector));
        show.setRoom(room);
        show.setDate(LocalDateTime.now().plusHours(1));

        when(showService.getShowById(showId)).thenReturn(show);
        when(roomService.getSectorById(sectorId)).thenReturn(sector);

        Ticket existing = new Ticket();
        existing.setSector(sector);
        existing.setStatus(TicketStatus.BOUGHT);

        when(ticketRepository.findByShowId(showId)).thenReturn(List.of(existing, existing, existing, existing));

        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sectorId);
        target.setQuantity(2);

        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(showId);
        request.setTargets(List.of(target));

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateForBuyTickets(request)
        );

        assertTrue(ex.getMessage().contains("Not enough standing capacity"));
    }

    @Test
    public void validateHold_shouldThrowIfSeatAlreadyOnHold() {
        Long showId = 1L;
        Long sectorId = 1L;
        Long seatId = 1L;

        Hold hold = new Hold();
        hold.setSectorId(sectorId);
        hold.setSeatId(seatId);
        hold.setValidUntil(LocalDateTime.now().plusMinutes(5));

        when(holdRepository.findByShowId(showId)).thenReturn(List.of(hold));

        Sector sector = new Sector();
        sector.setId(sectorId);

        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setSector(sector);

        Show show = new Show();
        show.setId(showId);
        show.setDate(LocalDateTime.now().plusHours(1));

        Room room = new Room();
        room.setSectors(Set.of(sector));
        show.setRoom(room);

        when(showService.getShowById(showId)).thenReturn(show);
        when(roomService.getSectorById(sectorId)).thenReturn(sector);
        when(roomService.getSeatById(seatId)).thenReturn(seat);

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateHold(showId, sectorId, seatId, 1L)
        );

        assertTrue(ex.getMessage().contains("Seat already on hold"));
    }


    @Test
    public void validateHold_shouldThrowIfStandingSectorFullyOnHold() {
        Long showId = 1L;
        Long sectorId = 1L;

        Hold hold1 = new Hold();
        hold1.setSectorId(sectorId);
        hold1.setSeatId(null);
        hold1.setValidUntil(LocalDateTime.now().plusMinutes(5));

        Hold hold2 = new Hold();
        hold2.setSectorId(sectorId);
        hold2.setSeatId(null);
        hold2.setValidUntil(LocalDateTime.now().plusMinutes(5));

        when(holdRepository.findByShowId(showId)).thenReturn(List.of(hold1, hold2));

        StandingSector sector = new StandingSector();
        sector.setId(sectorId);
        sector.setCapacity(2);
        when(roomService.getSectorById(sectorId)).thenReturn(sector);

        Show show = new Show();
        show.setId(showId);
        show.setDate(LocalDateTime.now().plusHours(1));
        Room room = new Room();
        room.setSectors(Set.of(sector));
        show.setRoom(room);
        when(showService.getShowById(showId)).thenReturn(show);

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateHold(showId, sectorId, null, 1L)
        );

        assertTrue(ex.getMessage().contains("Standing sector already fully on hold"));
    }

    @Test
    public void validateNoHoldsOn_shouldThrowIfSeatedTargetOnHoldByOtherUser() {
        Long showId = 1L;
        Long sectorId = 1L;
        Long seatId = 1L;

        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sectorId);
        target.setSeatId(seatId);

        Hold hold = new Hold();
        hold.setSectorId(sectorId);
        hold.setSeatId(seatId);
        hold.setValidUntil(LocalDateTime.now().plusMinutes(5));
        hold.setUserId(999L);

        when(holdRepository.findByShowId(showId)).thenReturn(List.of(hold));
        when(authFacade.getCurrentUserId()).thenReturn(1L);

        Sector sector = new Sector();
        sector.setId(sectorId);

        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setSector(sector);

        when(roomService.getSeatById(seatId)).thenReturn(seat);

        Room room = new Room();
        room.setSectors(Set.of(sector));

        Show show = new Show();
        show.setId(showId);
        show.setRoom(room);
        show.setDate(LocalDateTime.now().plusHours(1));

        when(showService.getShowById(showId)).thenReturn(show);
        when(roomService.getSectorById(sectorId)).thenReturn(sector);

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateForBuyTickets(new TicketRequestDto() {{
                setShowId(showId);
                setTargets(List.of(target));
            }})
        );

        assertTrue(ex.getMessage().contains("currently on hold"));
    }


    @Test
    public void validateNoHoldsOn_shouldThrowIfStandingOverCapacity() {
        Long showId = 1L;
        Long sectorId = 1L;

        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(sectorId);
        target.setQuantity(5);

        Hold hold = new Hold();
        hold.setSectorId(sectorId);
        hold.setSeatId(null);
        hold.setValidUntil(LocalDateTime.now().plusMinutes(5));
        hold.setUserId(999L);

        StandingSector standingSector = new StandingSector();
        standingSector.setId(sectorId);
        standingSector.setCapacity(10);

        Ticket ticket = new Ticket();
        ticket.setSector(standingSector);
        ticket.setSeat(null);
        ticket.setStatus(TicketStatus.BOUGHT);

        when(holdRepository.findByShowId(showId)).thenReturn(List.of(hold));
        when(authFacade.getCurrentUserId()).thenReturn(1L);
        when(ticketRepository.findByShowId(showId)).thenReturn(List.of(ticket));
        when(roomService.getSectorById(sectorId)).thenReturn(standingSector);

        Room room = new Room();
        room.setSectors(Set.of(standingSector));
        Show show = new Show();
        show.setId(showId);
        show.setRoom(room);
        show.setDate(LocalDateTime.now().plusHours(1));
        when(showService.getShowById(showId)).thenReturn(show);

        ticket.setStatus(TicketStatus.BOUGHT);
        hold.setValidUntil(LocalDateTime.now().plusMinutes(5));
        target.setQuantity(9);

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateForBuyTickets(new TicketRequestDto() {{
                setShowId(showId);
                setTargets(List.of(target));
            }})
        );

        assertTrue(ex.getMessage().contains("Not enough capacity"));
    }

    @Test
    public void validateHold_shouldThrowIfSectorNotInShowRoom() {
        Long showId = 1L;
        Long sectorId = 99L;
        Long seatId = 1L;

        Sector existingSector = new Sector();
        existingSector.setId(1L);
        Room room = new Room();
        room.setSectors(Set.of(existingSector));

        Show show = new Show();
        show.setId(showId);
        show.setRoom(room);
        show.setDate(LocalDateTime.now().plusHours(1));

        when(showService.getShowById(showId)).thenReturn(show);

        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setSector(existingSector);
        when(roomService.getSeatById(seatId)).thenReturn(seat);

        when(ticketRepository.findByShowId(showId)).thenReturn(List.of());

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateHold(showId, sectorId, seatId, 1L)
        );

        assertTrue(ex.getMessage().contains("Sector " + sectorId + " is not in room"));
    }

    @Test
    public void validateHold_shouldThrowIfSeatNotInSector() {
        Long showId = 1L;
        Long sectorId = 1L;
        Long seatId = 1L;

        Sector realSector = new Sector();
        realSector.setId(99L);
        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setSector(realSector);

        Sector wantedSector = new Sector();
        wantedSector.setId(sectorId);

        Room room = new Room();
        room.setSectors(Set.of(wantedSector));

        Show show = new Show();
        show.setId(showId);
        show.setRoom(room);
        show.setDate(LocalDateTime.now().plusHours(1));

        when(showService.getShowById(showId)).thenReturn(show);
        when(roomService.getSeatById(seatId)).thenReturn(seat);
        when(roomService.getSectorById(sectorId)).thenReturn(wantedSector);
        when(ticketRepository.findByShowId(showId)).thenReturn(List.of());

        SeatUnavailableException ex = assertThrows(SeatUnavailableException.class,
            () -> validator.validateHold(showId, sectorId, seatId, 1L)
        );

        assertTrue(ex.getMessage().contains("Seat " + seatId + " is not in sector " + sectorId));
    }

    @Test
    public void validateHold_shouldThrowIfShowNotFound() {
        Long showId = 1L;
        Long sectorId = 1L;
        Long seatId = 1L;

        when(showService.getShowById(showId)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
            () -> validator.validateHold(showId, sectorId, seatId, 1L)
        );

        assertTrue(ex.getMessage().contains("Show with id " + showId + " not found"));
    }

    @Test
    public void validateCheckoutAddress_shouldThrowIfPostalCodeBlank() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("Max");
        dto.setLastName("Mustermann");
        dto.setStreet("Main St");
        dto.setHousenumber("12");
        dto.setPostalCode("   ");
        dto.setCity("Vienna");
        dto.setCountry("Austria");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto)
        );

        assertTrue(ex.getMessage().contains("Postal code is given but blank"));
    }

    @Test
    public void validateCheckoutAddress_shouldThrowIfCityBlank() {
        TicketRequestDto dto = new TicketRequestDto();
        dto.setFirstName("Max");
        dto.setLastName("Mustermann");
        dto.setStreet("Main St");
        dto.setHousenumber("12");
        dto.setPostalCode("1010");
        dto.setCity(" "); // blank
        dto.setCountry("Austria");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> validator.validateCheckoutAddress(dto)
        );

        assertTrue(ex.getMessage().contains("City is given but blank"));
    }

}
