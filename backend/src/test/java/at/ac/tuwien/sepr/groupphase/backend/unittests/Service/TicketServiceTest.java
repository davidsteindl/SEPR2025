package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderGroupType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.SeatUnavailableException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.AuthenticationFacade;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import at.ac.tuwien.sepr.groupphase.backend.service.TicketService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class TicketServiceTest {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private ShowService showService;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EventLocationRepository eventLocationRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ArtistRepository artistRepository;
    @PersistenceContext
    EntityManager entityManager;

    @MockitoBean
    private AuthenticationFacade authenticationFacade;

    private EventLocation location;
    private Room testRoom;
    private SeatedSector seatedSector;
    private Seat seat;
    private StandingSector standingSector;
    private Show testShow;
    private Seat pastSeat;
    private String firstName;
    private String lastName;
    private String houseNumber;
    private String street;
    private String city;
    private String country;
    private String postalCode;

    @BeforeEach
    public void setUp() {
        // stub current user id
        when(authenticationFacade.getCurrentUserId()).thenReturn(1L);

        // create and save an event location
        location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Test Venue")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Test St")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.HALL)
            .build();
        eventLocationRepository.save(location);

        // build a room with one seated and one standing sector
        testRoom = new Room();
        testRoom.setName("Test Room");
        testRoom.setEventLocation(location);

        seatedSector = SeatedSector.SeatedSectorBuilder.aSeatedSector()
            .price(100)
            .room(testRoom)
            .seats(List.of())
            .build();
        // add one seat
        seat = new Seat();
        seat.setRowNumber(1);
        seat.setColumnNumber(1);
        seat.setDeleted(false);
        seatedSector.addSeat(seat);
        testRoom.addSector(seatedSector);

        standingSector = StandingSector.StandingSectorBuilder.aStandingSector()
            .price(50)
            .capacity(10)
            .room(testRoom)
            .build();
        testRoom.addSector(standingSector);

        testRoom = roomRepository.save(testRoom);


        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        Event testEvent = Event.EventBuilder.anEvent()
            .withName("Test Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("Test description")
            .withDateTime(now.plusDays(1))
            .withDuration(120)
            .withLocation(location)
            .build();
        eventRepository.save(testEvent);

        Artist testArtist = Artist.ArtistBuilder.anArtist()
            .withFirstname("John")
            .withLastname("Doe")
            .withStagename("JD")
            .withShows(null)
            .build();
        artistRepository.save(testArtist);

        LocalDateTime eventStart = testEvent.getDateTime();
        // create and persist a show in the future
        Show show = Show.ShowBuilder.aShow()
            .withName("Test Show")
            .withDuration(60)
            .withDate(eventStart.plusMinutes(10))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();
        try {
            testShow = showService.createShow(show);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }

        firstName = "John";
        lastName = "Doe";
        street = "Main Street";
        houseNumber = "10";
        city = "Vienna";
        country = "Austria";
        postalCode = "1010";
    }

    @Test
    @Transactional
    public void testBuySingleTicket_createsTicketWithCorrectAttributes() {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());

        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());
        request.setTargets(List.of(target));

        OrderDto orderDto = ticketService.buyTickets(request);
        TicketDto dto = orderDto.getTickets().getFirst();



        assertAll(
            () -> assertNotNull(orderDto.getId()),
            () -> assertEquals(1, orderDto.getTickets().size()),
            () -> assertEquals(testShow.getName(), dto.getShowName()),
            () -> assertEquals(seatedSector.getPrice(), dto.getPrice()),
            () -> assertEquals(seat.getId(), dto.getSeatId()),
            () -> assertEquals(seatedSector.getId(), dto.getSectorId()),
            () -> assertEquals(TicketStatus.BOUGHT, dto.getStatus()),
            () -> assertEquals(1, orderRepository.findAll().size()),
            () -> assertEquals(1, ticketRepository.findAll().size())
        );

    }

    @Test
    @Transactional
    public void testBuyMultipleTickets_multipleSeated_createsMultipleTicketsAndSingleOrder() {
        // select two seats
        Seat seat2 = new Seat();
        seat2.setRowNumber(1);
        seat2.setColumnNumber(2);
        seat2.setDeleted(false);
        seatedSector.addSeat(seat2);
        seat2 = roomRepository.save(testRoom).getSectors().stream()
            .filter(s -> s instanceof SeatedSector)
            .map(s -> ((SeatedSector) s).getSeats())
            .flatMap(List::stream)
            .filter(se -> !se.getId().equals(seat.getId()))
            .findFirst().orElseThrow();

        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());
        TicketTargetSeatedDto t1 = new TicketTargetSeatedDto();
        t1.setSectorId(seatedSector.getId());
        t1.setSeatId(seat.getId());
        TicketTargetSeatedDto t2 = new TicketTargetSeatedDto();
        t2.setSectorId(seatedSector.getId());
        t2.setSeatId(seat2.getId());
        request.setTargets(List.of(t1, t2));

        OrderDto orderDto = ticketService.buyTickets(request);

        assertAll(
            () -> assertNotNull(orderDto.getId()),
            () -> assertEquals(2, orderDto.getTickets().size()),
            () -> assertEquals(1, orderRepository.findAll().size(), "Only one order should be created"),
            () -> assertEquals(2, ticketRepository.findAll().size(), "Two tickets should be persisted")
        );

    }

    @Test
    @Transactional
    public void testBuyStandingTickets_createsMultipleTicketsWithNullSeat() {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());

        TicketTargetStandingDto standingTarget = new TicketTargetStandingDto();
        standingTarget.setSectorId(standingSector.getId());
        standingTarget.setQuantity(3);
        request.setTargets(List.of(standingTarget));

        OrderDto orderDto = ticketService.buyTickets(request);

        assertNotNull(orderDto.getId());
        assertEquals(3, orderDto.getTickets().size());
        orderDto.getTickets().forEach(dto -> {
            assertNull(dto.getSeatId());
            assertEquals(standingSector.getId(), dto.getSectorId());
            assertEquals(standingSector.getPrice(), dto.getPrice());
            assertEquals(TicketStatus.BOUGHT, dto.getStatus());
        });
        assertEquals(1, orderRepository.findAll().size());
        assertEquals(3, ticketRepository.findAll().size());
    }

    @Test
    @Transactional
    public void testReserveTickets_createsReservationOrderWithCorrectTypeAndExpiresAt() {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());

        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());
        request.setTargets(List.of(target));

        ReservationDto reservationDto = ticketService.reserveTickets(request);

        assertNotNull(reservationDto.getId());
        assertEquals(1, reservationDto.getTickets().size());
        TicketDto dto = reservationDto.getTickets().getFirst();
        assertEquals(TicketStatus.RESERVED, dto.getStatus());
        assertEquals(OrderType.RESERVATION, reservationDto.getOrderType());
        assertEquals(testShow.getDate().minusMinutes(30), reservationDto.getExpiresAt());

        assertEquals(1, orderRepository.findAll().size());
        assertEquals(1, ticketRepository.findAll().size());
    }

    @Test
    @Transactional
    public void testReserveOnAlreadyBoughtTicket_throwsValidationException() {
        // buy a ticket first
        TicketRequestDto buyReq = new TicketRequestDto();
        buyReq.setShowId(testShow.getId());
        TicketTargetSeatedDto buyTarget = new TicketTargetSeatedDto();
        buyTarget.setSectorId(seatedSector.getId());
        buyTarget.setSeatId(seat.getId());
        buyReq.setTargets(List.of(buyTarget));
        ticketService.buyTickets(buyReq);

        // now try to reserve the same seat
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        TicketTargetSeatedDto resTarget = new TicketTargetSeatedDto();
        resTarget.setSectorId(seatedSector.getId());
        resTarget.setSeatId(seat.getId());
        reserveReq.setTargets(List.of(resTarget));

        assertThrows(SeatUnavailableException.class, () -> {
            ticketService.reserveTickets(reserveReq);
        });
    }

    @Test
    @Transactional
    public void testBuyReservedTickets_onAlreadyBoughtTicket_throwsValidationException() {
        // buy a ticket first
        TicketRequestDto buyReq = new TicketRequestDto();
        buyReq.setShowId(testShow.getId());
        TicketTargetSeatedDto buyTarget = new TicketTargetSeatedDto();
        buyTarget.setSectorId(seatedSector.getId());
        buyTarget.setSeatId(seat.getId());
        buyReq.setTargets(List.of(buyTarget));
        OrderDto boughtOrder = ticketService.buyTickets(buyReq);

        // attempt to reserva a already bought ticket
        Long boughtTicketId = boughtOrder.getTickets().getFirst().getId();
        assertThrows(IllegalArgumentException.class, () -> {
            ticketService.buyReservedTickets(List.of(boughtTicketId));
        });
    }

    @Test
    @Transactional
    public void testBuyReservedTickets_convertsReservationToNewOrder() {
        // reserve a ticket
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        TicketTargetSeatedDto resTarget = new TicketTargetSeatedDto();
        resTarget.setSectorId(seatedSector.getId());
        resTarget.setSeatId(seat.getId());
        reserveReq.setTargets(List.of(resTarget));
        ReservationDto reservation = ticketService.reserveTickets(reserveReq);

        assertEquals(OrderType.RESERVATION, reservation.getOrderType());
        Long reservationOrderId = reservation.getId();
        Long reservedTicketId = reservation.getTickets().getFirst().getId();

        // buy the reserved ticket
        OrderDto newOrder = ticketService.buyReservedTickets(List.of(reservedTicketId));

        // new order should be type ORDER, contain that ticket, and ticket status updated
        assertEquals(OrderType.ORDER, newOrder.getOrderType());
        assertEquals(1, newOrder.getTickets().size());
        TicketDto bought = newOrder.getTickets().getFirst();
        assertEquals(reservedTicketId, bought.getId());
        assertEquals(TicketStatus.BOUGHT, bought.getStatus());

        // repository now has two orders
        assertEquals(2, orderRepository.findAll().size());

        // old reservation must no longer own that ticket
        var oldOrderOpt = orderRepository.findById(reservationOrderId);
        assertTrue(oldOrderOpt.isPresent());
        assertTrue(oldOrderOpt.get().getTickets().isEmpty());
    }

    @Test
    @Transactional
    public void testBuyReservedTickets_onExpiredReservation_throwsReservationExpiredException() {
        // Reserve a ticket
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        TicketTargetSeatedDto resTarget = new TicketTargetSeatedDto();
        resTarget.setSectorId(seatedSector.getId());
        resTarget.setSeatId(seat.getId());
        reserveReq.setTargets(List.of(resTarget));
        ReservationDto reservation = ticketService.reserveTickets(reserveReq);

        Long reservedTicketId = reservation.getTickets().getFirst().getId();

        // Simulate expiration by marking the ticket EXPIRED directly in the repo
        var ticketEntity = ticketRepository.findById(reservedTicketId)
            .orElseThrow();
        ticketEntity.setStatus(TicketStatus.EXPIRED);
        ticketRepository.save(ticketEntity);

        // Attempt to buy the now-expired reservation should throw
        assertThrows(IllegalArgumentException.class, () -> {
            ticketService.buyReservedTickets(List.of(reservedTicketId));
        });
    }

    @Test
    @Transactional
    public void testCancelReservations_releasesSeat_forNewReservation() {
        // Reserve a seat
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());
        reserveReq.setTargets(List.of(target));
        ReservationDto reservation = ticketService.reserveTickets(reserveReq);

        Long reservedTicketId = reservation.getTickets().getFirst().getId();
        Long reservationOrderId = reservation.getId();

        // Cancel that reservation
        List<TicketDto> cancelled = ticketService.cancelReservations(List.of(reservedTicketId));
        assertEquals(1, cancelled.size());
        assertEquals(TicketStatus.CANCELLED, cancelled.getFirst().getStatus());

        // Now we should be able to reserve the same seat again
        ReservationDto newRes = ticketService.reserveTickets(reserveReq);
        assertNotNull(newRes.getId());
        assertNotEquals(reservationOrderId, newRes.getId());
        assertEquals(TicketStatus.RESERVED, newRes.getTickets().getFirst().getStatus());
    }

    @Test
    @Transactional
    public void testRefundTickets_freesSeat_forRebuy() {
        // Buy a seat
        TicketRequestDto buyReq = new TicketRequestDto();
        buyReq.setShowId(testShow.getId());
        TicketTargetSeatedDto buyTarget = new TicketTargetSeatedDto();
        buyTarget.setSectorId(seatedSector.getId());
        buyTarget.setSeatId(seat.getId());
        buyReq.setTargets(List.of(buyTarget));
        OrderDto buyOrder = ticketService.buyTickets(buyReq);

        Long boughtTicketId = buyOrder.getTickets().getFirst().getId();

        // Refund it
        List<TicketDto> refunded = ticketService.refundTickets(List.of(boughtTicketId));
        assertEquals(1, refunded.size());
        assertEquals(TicketStatus.REFUNDED, refunded.getFirst().getStatus());

        // buy the same seat again
        OrderDto rebuy = ticketService.buyTickets(buyReq);
        assertNotNull(rebuy.getId());
        assertNotEquals(buyOrder.getId(), rebuy.getId());
        TicketDto newTicket = rebuy.getTickets().getFirst();
        assertEquals(seat.getId(), newTicket.getSeatId());
        assertEquals(TicketStatus.BOUGHT, newTicket.getStatus());
    }

    @Test
    @Transactional
    public void testCancelReservations_updatesTicketStatusToCancelled() {
        // Reserve first
        TicketRequestDto req = new TicketRequestDto();
        req.setShowId(testShow.getId());
        TicketTargetSeatedDto t = new TicketTargetSeatedDto();
        t.setSectorId(seatedSector.getId());
        t.setSeatId(seat.getId());
        req.setTargets(List.of(t));
        ReservationDto res = ticketService.reserveTickets(req);

        Long id = res.getTickets().getFirst().getId();
        List<TicketDto> dtos = ticketService.cancelReservations(List.of(id));

        assertEquals(1, dtos.size());
        assertEquals(TicketStatus.CANCELLED, dtos.getFirst().getStatus());
    }

    @Test
    @Transactional
    public void testRefundTickets_updatesTicketStatusToRefunded() {
        // Buy first
        TicketRequestDto req = new TicketRequestDto();
        req.setShowId(testShow.getId());
        TicketTargetSeatedDto t = new TicketTargetSeatedDto();
        t.setSectorId(seatedSector.getId());
        t.setSeatId(seat.getId());
        req.setTargets(List.of(t));
        OrderDto ord = ticketService.buyTickets(req);

        Long id = ord.getTickets().getFirst().getId();
        List<TicketDto> dtos = ticketService.refundTickets(List.of(id));

        assertEquals(1, dtos.size());
        assertEquals(TicketStatus.REFUNDED, dtos.getFirst().getStatus());
    }

    @Test
    @Transactional
    public void getOrdersForUser_futureOrders_returnsOnlyFutureOrders() {
        // Buy ticket for future show
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());

        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());
        request.setTargets(List.of(target));

        ticketService.buyTickets(request);

        var result = ticketService.getOrdersForUser(1L, OrderType.ORDER, false, Pageable.ofSize(10));

        assertAll(
            () -> assertEquals(1, result.getTotalElements(), "Should return one future order"),
            () -> assertEquals(OrderType.ORDER, result.getContent().getFirst().getOrderType()),
            () -> assertFalse(result.getContent().getFirst().getShowDate().isBefore(LocalDateTime.now()))
        );
    }

    @Test
    @Transactional
    public void getOrdersForUser_reservations_returnsOnlyActiveReservations() {
        Seat reserveSeat = new Seat();
        reserveSeat.setRowNumber(2);
        reserveSeat.setColumnNumber(42);
        reserveSeat.setDeleted(false);
        seatedSector.addSeat(reserveSeat);

        reserveSeat = roomRepository.save(testRoom).getSectors().stream()
            .filter(s -> s instanceof SeatedSector)
            .map(s -> ((SeatedSector) s).getSeats())
            .flatMap(List::stream)
            .filter(se -> se.getColumnNumber() == 42)
            .findFirst()
            .orElseThrow();

        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());

        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(reserveSeat.getId());
        request.setTargets(List.of(target));

        ticketService.reserveTickets(request);

        var result = ticketService.getOrdersForUser(1L, OrderType.RESERVATION, false, Pageable.ofSize(10));

        assertAll(
            () -> assertEquals(1, result.getTotalElements(), "Should return one active reservation"),
            () -> assertEquals(OrderType.RESERVATION, result.getContent().getFirst().getOrderType()),
            () -> assertFalse(result.getContent().getFirst().getShowDate().isBefore(LocalDateTime.now()))
        );
    }

    @Test
    @Transactional
    public void getOrdersForUser_pastReservations_returnsEmptyList() {
        var result = ticketService.getOrdersForUser(1L, OrderType.RESERVATION, true, Pageable.ofSize(10));

        assertAll(
            () -> assertEquals(0, result.getTotalElements(), "Should return no past reservations")
        );
    }

    @Test
    @Transactional
    public void testGetOrderWithTicketsById_returnsFullOrder() {
        // Create order
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());
        request.setTargets(List.of(target));

        OrderDto createdOrder = ticketService.buyTickets(request);
        Long orderId = createdOrder.getId();

        // Retrieve full order with tickets
        OrderDto fetched = ticketService.getOrderWithTicketsById(orderId);

        assertAll(
            () -> assertNotNull(fetched),
            () -> assertEquals(orderId, fetched.getId()),
            () -> assertEquals(testShow.getName(), fetched.getShowName()),
            () -> assertEquals(testShow.getDate(), fetched.getShowDate()),
            () -> assertEquals(location.getName(), fetched.getLocationName()),
            () -> assertNotNull(fetched.getTickets()),
            () -> assertEquals(1, fetched.getTickets().size()),
            () -> assertEquals(TicketStatus.BOUGHT, fetched.getTickets().getFirst().getStatus())
        );
    }

    @Test
    @Transactional
    public void testReserveTicketsGrouped_createsReservationWithGroup() {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());

        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());
        request.setTargets(List.of(target));

        ReservationDto reservationDto = ticketService.reserveTicketsGrouped(request);

        assertAll(
            () -> assertNotNull(reservationDto.getId()),
            () -> assertEquals(1, reservationDto.getTickets().size()),
            () -> assertEquals(OrderType.RESERVATION, reservationDto.getOrderType()),
            () -> assertEquals(TicketStatus.RESERVED, reservationDto.getTickets().getFirst().getStatus()),
            () -> assertNotNull(orderRepository.findById(reservationDto.getId()).get().getOrderGroup())
        );
    }

    @Test
    @Transactional
    public void testRefundTicketsGroup_createsRefundAndKeepsRemaining() {
        // Setup - buy two tickets
        Seat seat2 = new Seat();
        seat2.setRowNumber(2);
        seat2.setColumnNumber(2);
        seat2.setDeleted(false);
        seatedSector.addSeat(seat2);
        seat2 = roomRepository.save(testRoom).getSectors().stream()
            .filter(s -> s instanceof SeatedSector)
            .map(s -> ((SeatedSector) s).getSeats())
            .flatMap(List::stream)
            .filter(se -> se.getColumnNumber() == 2)
            .findFirst().orElseThrow();

        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());
        TicketTargetSeatedDto t1 = new TicketTargetSeatedDto();
        t1.setSectorId(seatedSector.getId());
        t1.setSeatId(seat.getId());
        TicketTargetSeatedDto t2 = new TicketTargetSeatedDto();
        t2.setSectorId(seatedSector.getId());
        t2.setSeatId(seat2.getId());
        request.setTargets(List.of(t1, t2));

        OrderDto originalOrder = ticketService.buyTickets(request);
        List<TicketDto> originalTickets = originalOrder.getTickets();
        assertEquals(2, originalTickets.size());

        Long toRefundId = originalTickets.getFirst().getId();
        List<TicketDto> refunded = ticketService.refundTicketsGroup(List.of(toRefundId));

        assertAll(
            () -> assertEquals(1, refunded.size(), "Exactly one ticket should be refunded"),
            () -> assertEquals(TicketStatus.REFUNDED, refunded.getFirst().getStatus(), "Refunded ticket should have status REFUNDED"),
            () -> assertNotNull(refunded.getFirst().getOriginalTicketId(), "Refunded ticket should reference original ticket"),
            () -> assertEquals(toRefundId, refunded.getFirst().getOriginalTicketId(), "Refunded ticket should refer to the correct original ticket"),
            () -> assertEquals(4, ticketRepository.findAll().size(), "Total ticket count should be 4: 2 original + 1 refund + 1 new"),
            () -> assertEquals(3, orderRepository.findAll().size(), "Three orders: 1 original + 1 refund + 1 new")
        );
    }


    @Test
    @Transactional
    public void testCheckoutTickets_createsFullOrderWithReservedAndNew() throws ValidationException {
        // First, reserve a ticket
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        TicketTargetSeatedDto reserveTarget = new TicketTargetSeatedDto();
        reserveTarget.setSectorId(seatedSector.getId());
        reserveTarget.setSeatId(seat.getId());
        reserveReq.setTargets(List.of(reserveTarget));

        ReservationDto reservation = ticketService.reserveTickets(reserveReq);
        Long reservedTicketId = reservation.getTickets().getFirst().getId();

        // Then checkout that reservation
        CheckoutRequestDto checkout = new CheckoutRequestDto();
        checkout.setShowId(testShow.getId());
        checkout.setFirstName(firstName);
        checkout.setLastName(lastName);
        checkout.setStreet(street);
        checkout.setHousenumber(houseNumber);
        checkout.setCity(city);
        checkout.setCountry(country);
        checkout.setPostalCode(postalCode);
        checkout.setReservedTicketIds(List.of(reservedTicketId));
        checkout.setCardNumber("4111111111111111");
        checkout.setSecurityCode("123");
        checkout.setExpirationDate("12/28");
        // Add one new standing ticket
        TicketTargetStandingDto standing = new TicketTargetStandingDto();
        standing.setSectorId(standingSector.getId());
        standing.setQuantity(1);
        checkout.setTargets(List.of(standing));

        OrderGroupDto groupDto = ticketService.checkoutTickets(checkout);

        assertAll(
            () -> assertNotNull(groupDto.getId()),
            () -> assertEquals(1, groupDto.getOrders().size()),
            () -> assertEquals(2, groupDto.getOrders().getFirst().getTickets().size()),
            () -> assertEquals("John", groupDto.getOrders().getFirst().getFirstName()),
            () -> assertEquals("Doe", groupDto.getOrders().getFirst().getLastName()),
            () -> assertTrue(groupDto.getTotalPrice() > 0)
        );
    }


    @Test
    @Transactional
    public void testCheckoutTickets_withInvalidPaymentData_throwsValidationException() {
        CheckoutRequestDto checkout = new CheckoutRequestDto();
        checkout.setShowId(testShow.getId());
        checkout.setFirstName(firstName);
        checkout.setLastName(lastName);
        checkout.setStreet(street);
        checkout.setHousenumber(houseNumber);
        checkout.setCity(city);
        checkout.setCountry(country);
        checkout.setPostalCode(postalCode);
        checkout.setCardNumber("invalid-card");
        checkout.setSecurityCode("123");
        checkout.setExpirationDate("20/03");

        checkout.setShowId(testShow.getId());

        assertThrows(ValidationException.class, () -> {
            ticketService.checkoutTickets(checkout);
        });
    }


    @Test
    @Transactional
    public void testReserveTicketsGrouped_withInvalidSector_throwsValidationException() {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());

        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(-1L);
        target.setSeatId(seat.getId());

        request.setTargets(List.of(target));

        assertThrows(SeatUnavailableException.class, () -> {
            ticketService.reserveTicketsGrouped(request);
        });
    }


    @Test
    @Transactional
    public void testReserveTicketsGrouped_withTakenSeat_throwsSeatUnavailableException() {
        TicketRequestDto request1 = new TicketRequestDto();
        request1.setShowId(testShow.getId());
        TicketTargetSeatedDto t1 = new TicketTargetSeatedDto();
        t1.setSectorId(seatedSector.getId());
        t1.setSeatId(seat.getId());
        request1.setTargets(List.of(t1));
        ticketService.reserveTicketsGrouped(request1);

        TicketRequestDto request2 = new TicketRequestDto();
        request2.setShowId(testShow.getId());
        TicketTargetSeatedDto t2 = new TicketTargetSeatedDto();
        t2.setSectorId(seatedSector.getId());
        t2.setSeatId(seat.getId());
        request2.setTargets(List.of(t2));

        assertThrows(SeatUnavailableException.class, () -> {
            ticketService.reserveTicketsGrouped(request2);
        });
    }


    @Test
    @Transactional
    public void testGetOrderGroupsForUser_withReservedGroup_returnsCorrectOrderGroupDto() {
        // Prepare reservation
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());

        TicketTargetSeatedDto reserveTarget = new TicketTargetSeatedDto();
        reserveTarget.setSectorId(seatedSector.getId());
        reserveTarget.setSeatId(seat.getId());
        reserveReq.setTargets(List.of(reserveTarget));

        // Call method that creates group + reservation
        ReservationDto reservation = ticketService.reserveTicketsGrouped(reserveReq);

        entityManager.flush();
        entityManager.clear();
        // Clear persistence context to force reload

        // Call the method under test
        var page = ticketService.getOrderGroupsForUser(OrderGroupType.RESERVED, Pageable.ofSize(10));

        assertAll(
            () -> assertEquals(1, page.getTotalElements(), "Should return one reserved order group"),
            () -> assertEquals(testShow.getName(), page.getContent().getFirst().getShowName()),
            () -> assertEquals(location.getName(), page.getContent().getFirst().getLocationName()),
            () -> assertEquals(testShow.getDate(), page.getContent().getFirst().getShowDate()),
            () -> assertEquals(1, page.getContent().getFirst().getOrders().size()),
            () -> assertEquals(OrderType.RESERVATION, page.getContent().getFirst().getOrders().getFirst().getOrderType())
        );
    }


    @Test
    @Transactional
    public void testGetOrderGroupsForUser_afterPartialRefund() throws ValidationException {
        // Buy two tickets
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(standingSector.getId());
        target.setQuantity(2);

        CheckoutRequestDto checkout = new CheckoutRequestDto();
        checkout.setShowId(testShow.getId());
        checkout.setTargets(List.of(target));
        checkout.setCardNumber("4111111111111111");
        checkout.setSecurityCode("123");
        checkout.setExpirationDate("12/30");
        checkout.setFirstName(firstName);
        checkout.setLastName(lastName);
        checkout.setStreet(street);
        checkout.setHousenumber(houseNumber);
        checkout.setCity(city);
        checkout.setPostalCode(postalCode);
        checkout.setCountry(country);

        OrderGroupDto checkoutGroup = ticketService.checkoutTickets(checkout);
        Long groupId = checkoutGroup.getId();

        // Refund one ticket
        List<TicketDto> allTickets = checkoutGroup.getOrders().getFirst().getTickets();
        assertEquals(2, allTickets.size(), "Es sollten zwei Tickets gekauft worden sein");

        Long ticketIdToRefund = allTickets.getFirst().getId();
        List<TicketDto> refunded = ticketService.refundTicketsGroup(List.of(ticketIdToRefund));
        assertEquals(1, refunded.size(), "Es sollte ein Ticket refundiert worden sein");

        entityManager.flush();
        entityManager.clear();

        var result = ticketService.getOrderGroupsForUser(OrderGroupType.PURCHASED, Pageable.ofSize(10));

        assertAll(
            () -> assertEquals(1, result.getTotalElements(), "There should be exactly one OrderGroup"),
            () -> assertEquals(groupId, result.getContent().getFirst().getId(), "The group ID should match the original checkout group"),
            () -> assertEquals(3, result.getContent().getFirst().getOrders().size(), "The group should contain the old, the new and the refunded Order"),
            () -> assertTrue(
                result.getContent().getFirst().getOrders().stream().anyMatch(o -> o.getOrderType() == OrderType.ORDER),
                "An ORDER should be present in the group"
            ),
            () -> assertTrue(
                result.getContent().getFirst().getOrders().stream().anyMatch(o -> o.getOrderType() == OrderType.REFUND),
                "A REFUND should be present in the group"
            )
        );
    }

}



