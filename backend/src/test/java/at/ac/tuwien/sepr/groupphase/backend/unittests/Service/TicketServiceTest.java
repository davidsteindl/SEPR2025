package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderGroupType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.*;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.OrderGroup;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.SeatUnavailableException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderGroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.AuthenticationFacade;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import at.ac.tuwien.sepr.groupphase.backend.service.TicketService;
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
    @Autowired
    private OrderGroupRepository orderGroupRepository;

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

    private TicketRequestDto createBuyRequest(List<TicketTargetDto> targets) {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());
        request.setTargets(targets);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setStreet(street);
        request.setHousenumber(houseNumber);
        request.setCity(city);
        request.setCountry(country);
        request.setPostalCode(postalCode);
        request.setCardNumber("4242424242424242");
        request.setExpirationDate("12/30");
        request.setSecurityCode("123");
        return request;
    }

    @Test
    @Transactional
    public void testBuySingleTicket_createsTicketWithCorrectAttributes() throws ValidationException {
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());
        TicketRequestDto request = createBuyRequest(List.of(target));

        OrderDto orderDto = ticketService.buyTickets(request);
        TicketDto dto = orderDto.getTickets().getFirst();

        var orderEntity = orderRepository.findById(orderDto.getId()).orElseThrow();

        assertAll(
            () -> assertNotNull(orderDto.getId()),
            () -> assertEquals(1, orderDto.getTickets().size()),
            () -> assertEquals(testShow.getName(), dto.getShowName()),
            () -> assertEquals(seatedSector.getPrice(), dto.getPrice()),
            () -> assertEquals(seat.getId(), dto.getSeatId()),
            () -> assertEquals(seatedSector.getId(), dto.getSectorId()),
            () -> assertEquals(TicketStatus.BOUGHT, dto.getStatus()),
            () -> assertEquals(firstName, orderEntity.getFirstName()),
            () -> assertEquals(lastName, orderEntity.getLastName()),
            () -> assertEquals(street, orderEntity.getStreet()),
            () -> assertEquals(houseNumber, orderEntity.getHousenumber()),
            () -> assertEquals(postalCode, orderEntity.getPostalCode()),
            () -> assertEquals(city, orderEntity.getCity()),
            () -> assertEquals(country, orderEntity.getCountry()),
            () -> assertNotNull(orderEntity.getOrderGroup()),
            () -> assertEquals(1L, orderEntity.getOrderGroup().getUserId()),
            () -> assertEquals(1, orderRepository.findAll().size()),
            () -> assertEquals(1, ticketRepository.findAll().size())
        );
    }

    @Test
    @Transactional
    public void testBuyStandingTicket_savesAddressAndGroupCorrectly() throws ValidationException {
        TicketTargetStandingDto standingTarget = new TicketTargetStandingDto();
        standingTarget.setSectorId(standingSector.getId());
        standingTarget.setQuantity(2);
        TicketRequestDto request = createBuyRequest(List.of(standingTarget));

        OrderDto orderDto = ticketService.buyTickets(request);
        var orderEntity = orderRepository.findById(orderDto.getId()).orElseThrow();

        assertAll(
            () -> assertEquals(2, orderDto.getTickets().size()),
            () -> assertEquals(firstName, orderEntity.getFirstName()),
            () -> assertEquals(lastName, orderEntity.getLastName()),
            () -> assertEquals(street, orderEntity.getStreet()),
            () -> assertEquals(houseNumber, orderEntity.getHousenumber()),
            () -> assertEquals(postalCode, orderEntity.getPostalCode()),
            () -> assertEquals(city, orderEntity.getCity()),
            () -> assertEquals(country, orderEntity.getCountry()),
            () -> assertNotNull(orderEntity.getOrderGroup()),
            () -> assertEquals(1L, orderEntity.getOrderGroup().getUserId())
        );
    }

    @Test
    @Transactional
    public void testBuyMultipleTickets_multipleSeated_createsMultipleTicketsAndSingleOrder() throws ValidationException {
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

        // create targets
        TicketTargetSeatedDto t1 = new TicketTargetSeatedDto();
        t1.setSectorId(seatedSector.getId());
        t1.setSeatId(seat.getId());

        TicketTargetSeatedDto t2 = new TicketTargetSeatedDto();
        t2.setSectorId(seatedSector.getId());
        t2.setSeatId(seat2.getId());

        // use helper method
        TicketRequestDto request = createBuyRequest(List.of(t1, t2));

        OrderDto orderDto = ticketService.buyTickets(request);

        assertAll(
            () -> assertNotNull(orderDto.getId()),
            () -> assertEquals(2, orderDto.getTickets().size()),
            () -> assertEquals(1, orderRepository.findAll().size(), "Only one order should be created"),
            () -> assertEquals(2, ticketRepository.findAll().size(), "Two tickets should be persisted"),
            () -> assertEquals(firstName, orderRepository.findAll().getFirst().getFirstName(), "First name should be set on order"),
            () -> assertNotNull(orderRepository.findAll().getFirst().getOrderGroup(), "Order group should be created")
        );
    }


    @Test
    @Transactional
    public void testBuyStandingTickets_createsMultipleTicketsWithNullSeat() throws ValidationException {
        // prepare standing ticket target
        TicketTargetStandingDto standingTarget = new TicketTargetStandingDto();
        standingTarget.setSectorId(standingSector.getId());
        standingTarget.setQuantity(3);

        // use helper method
        TicketRequestDto request = createBuyRequest(List.of(standingTarget));

        OrderDto orderDto = ticketService.buyTickets(request);

        assertAll(
            () -> assertNotNull(orderDto.getId()),
            () -> assertEquals(3, orderDto.getTickets().size(), "Should create 3 tickets"),
            () -> assertEquals(1, orderRepository.findAll().size(), "Should create 1 order"),
            () -> assertEquals(3, ticketRepository.findAll().size(), "Should persist 3 tickets"),
            () -> assertEquals(firstName, orderRepository.findAll().getFirst().getFirstName(), "First name should be mapped to order"),
            () -> assertNotNull(orderRepository.findAll().getFirst().getOrderGroup(), "Order group should be assigned")
        );

        orderDto.getTickets().forEach(dto -> {
            assertAll(
                () -> assertNull(dto.getSeatId(), "Standing ticket should not have a seat"),
                () -> assertEquals(standingSector.getId(), dto.getSectorId()),
                () -> assertEquals(standingSector.getPrice(), dto.getPrice()),
                () -> assertEquals(TicketStatus.BOUGHT, dto.getStatus())
            );
        });
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
    public void testReserveOnAlreadyBoughtTicket_throwsValidationException() throws ValidationException {
        // buy a ticket first
        TicketTargetSeatedDto buyTarget = new TicketTargetSeatedDto();
        buyTarget.setSectorId(seatedSector.getId());
        buyTarget.setSeatId(seat.getId());
        TicketRequestDto buyReq = createBuyRequest(List.of(buyTarget));
        ticketService.buyTickets(buyReq);

        // now try to reserve the same seat
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        reserveReq.setTargets(List.of(buyTarget));

        assertThrows(SeatUnavailableException.class, () -> {
            ticketService.reserveTickets(reserveReq);
        });
    }

    @Test
    @Transactional
    public void testBuyReservedTickets_onAlreadyBoughtTicket_throwsValidationException() throws ValidationException {
        // buy a ticket first
        TicketTargetSeatedDto buyTarget = new TicketTargetSeatedDto();
        buyTarget.setSectorId(seatedSector.getId());
        buyTarget.setSeatId(seat.getId());
        TicketRequestDto buyReq = createBuyRequest(List.of(buyTarget));
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

        var oldOrderOpt = orderRepository.findById(reservationOrderId);
        var newOrderOpt = orderRepository.findById(newOrder.getId());

        assertAll(
            // Order type and ticket state
            () -> assertEquals(OrderType.ORDER, newOrder.getOrderType()),
            () -> assertEquals(1, newOrder.getTickets().size()),
            () -> assertEquals(reservedTicketId, newOrder.getTickets().getFirst().getId()),
            () -> assertEquals(TicketStatus.BOUGHT, newOrder.getTickets().getFirst().getStatus()),

            // Repository state
            () -> assertEquals(2, orderRepository.findAll().size()),
            () -> assertTrue(oldOrderOpt.isPresent(), "Old reservation order should still exist"),
            () -> assertTrue(oldOrderOpt.get().getTickets().isEmpty(), "Old reservation should no longer hold the ticket"),

            // Check that both orders are in the same OrderGroup
            () -> assertTrue(newOrderOpt.isPresent(), "New order should exist"),
            () -> assertEquals(
                oldOrderOpt.get().getOrderGroup().getId(),
                newOrderOpt.get().getOrderGroup().getId(),
                "Transferred orders should belong to the same OrderGroup"
            )
        );
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
        Long initialOrderGroupId = orderRepository.findById(reservationOrderId)
            .orElseThrow()
            .getOrderGroup()
            .getId();

        // Cancel that reservation
        List<TicketDto> cancelled = ticketService.cancelReservations(List.of(reservedTicketId));
        assertAll(
            () -> assertEquals(1, cancelled.size()),
            () -> assertEquals(TicketStatus.CANCELLED, cancelled.getFirst().getStatus())
        );

        // Now we should be able to reserve the same seat again
        ReservationDto newRes = ticketService.reserveTickets(reserveReq);

        Long newReservationOrderId = newRes.getId();
        Long newOrderGroupId = orderRepository.findById(newReservationOrderId)
            .orElseThrow()
            .getOrderGroup()
            .getId();

        assertAll(
            () -> assertNotNull(newRes.getId()),
            () -> assertNotEquals(reservationOrderId, newRes.getId()),
            () -> assertEquals(TicketStatus.RESERVED, newRes.getTickets().getFirst().getStatus()),
            () -> assertNotEquals(initialOrderGroupId, newOrderGroupId, "New reservation should have a new OrderGroup")
        );
    }


    @Test
    @Transactional
    public void testRefundTickets_freesSeat_forRebuy() throws ValidationException {
        // Buy a seat
        TicketRequestDto buyReq = new TicketRequestDto();
        buyReq.setShowId(testShow.getId());

        TicketTargetSeatedDto buyTarget = new TicketTargetSeatedDto();
        buyTarget.setSectorId(seatedSector.getId());
        buyTarget.setSeatId(seat.getId());
        buyReq.setTargets(List.of(buyTarget));
        buyReq.setFirstName(firstName);
        buyReq.setLastName(lastName);
        buyReq.setStreet(street);
        buyReq.setHousenumber(houseNumber);
        buyReq.setCity(city);
        buyReq.setCountry(country);
        buyReq.setPostalCode(postalCode);
        buyReq.setCardNumber("4242424242424242");
        buyReq.setExpirationDate("12/30");
        buyReq.setSecurityCode("123");

        OrderDto buyOrder = ticketService.buyTickets(buyReq);
        Long boughtTicketId = buyOrder.getTickets().getFirst().getId();

        Long originalOrderGroupId = orderRepository.findById(buyOrder.getId())
            .orElseThrow()
            .getOrderGroup()
            .getId();

        // Refund it
        List<TicketDto> refunded = ticketService.refundTickets(List.of(boughtTicketId));

        assertAll(
            () -> assertEquals(1, refunded.size()),
            () -> assertEquals(TicketStatus.REFUNDED, refunded.getFirst().getStatus())
        );

        // buy the same seat again
        OrderDto rebuy = ticketService.buyTickets(buyReq);
        Long newOrderGroupId = orderRepository.findById(rebuy.getId())
            .orElseThrow()
            .getOrderGroup()
            .getId();

        TicketDto newTicket = rebuy.getTickets().getFirst();

        assertAll(
            () -> assertNotNull(rebuy.getId()),
            () -> assertNotEquals(buyOrder.getId(), rebuy.getId(), "New order should have different ID"),
            () -> assertEquals(seat.getId(), newTicket.getSeatId()),
            () -> assertEquals(TicketStatus.BOUGHT, newTicket.getStatus()),
            () -> assertNotEquals(originalOrderGroupId, newOrderGroupId, "Rebuy should create new OrderGroup"),
            () -> assertEquals(firstName, rebuy.getFirstName()),
            () -> assertEquals(lastName, rebuy.getLastName()),
            () -> assertEquals(street, rebuy.getStreet()),
            () -> assertEquals(houseNumber, rebuy.getHousenumber()),
            () -> assertEquals(city, rebuy.getCity()),
            () -> assertEquals(postalCode, rebuy.getPostalCode()),
            () -> assertEquals(country, rebuy.getCountry())
        );
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
    public void testRefundTickets_updatesTicketStatusToRefunded() throws ValidationException {
        TicketTargetSeatedDto t = new TicketTargetSeatedDto();
        t.setSectorId(seatedSector.getId());
        t.setSeatId(seat.getId());

        TicketRequestDto req = createBuyRequest(List.of(t));
        OrderDto ord = ticketService.buyTickets(req);

        Long ticketId = ord.getTickets().getFirst().getId();

        // Refund
        List<TicketDto> dtos = ticketService.refundTickets(List.of(ticketId));

        assertAll(
            () -> assertEquals(1, dtos.size(), "Exactly one ticket should be refunded"),
            () -> assertEquals(TicketStatus.REFUNDED, dtos.getFirst().getStatus(), "Ticket status should be REFUNDED"),
            () -> assertEquals(ticketId, dtos.getFirst().getId(), "Refunded ticket ID should match the original")
        );
    }


    @Test
    @Transactional
    public void testRefundedPurchase_createsTwoOrdersInOrderGroup() throws ValidationException {
        // Buy ticket
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());
        TicketRequestDto request = createBuyRequest(List.of(target));
        OrderDto initialOrder = ticketService.buyTickets(request);
        Long ticketId = initialOrder.getTickets().getFirst().getId();

        // Refund ticket
        ticketService.refundTickets(List.of(ticketId));

        // Reload OrderGroup with fetched orders
        Long groupId = orderRepository.findById(initialOrder.getId())
            .orElseThrow()
            .getOrderGroup()
            .getId();
        OrderGroup group = orderGroupRepository.findByIdWithOrders(groupId)
            .orElseThrow();

        assertAll(
            () -> assertEquals(2, group.getOrders().size(), "Refund should result in two orders in the group")
        );
    }

    /**
     * Tests that buying two tickets and refunding only one results in three orders in the same order group:
     * - One original ORDER
     * - One REFUND for the refunded ticket
     * - One ORDER still containing the other bought ticket
     */
    @Test
    @Transactional
    public void testPartialRefund_createsThreeOrdersInOrderGroup() throws ValidationException {
        // Create two seated tickets
        Seat secondSeat = new Seat();
        secondSeat.setRowNumber(1);
        secondSeat.setColumnNumber(2);
        secondSeat.setDeleted(false);
        seatedSector.addSeat(secondSeat);
        secondSeat = roomRepository.save(testRoom).getSectors().stream()
            .filter(s -> s instanceof SeatedSector)
            .map(s -> ((SeatedSector) s).getSeats())
            .flatMap(List::stream)
            .filter(se -> !se.getId().equals(seat.getId()))
            .findFirst().orElseThrow();

        TicketTargetSeatedDto t1 = new TicketTargetSeatedDto();
        t1.setSectorId(seatedSector.getId());
        t1.setSeatId(seat.getId());

        TicketTargetSeatedDto t2 = new TicketTargetSeatedDto();
        t2.setSectorId(seatedSector.getId());
        t2.setSeatId(secondSeat.getId());

        TicketRequestDto request = createBuyRequest(List.of(t1, t2));

        // Buy tickets
        OrderDto initialOrder = ticketService.buyTickets(request);
        List<TicketDto> boughtTickets = initialOrder.getTickets();

        Long refundedTicketId = boughtTickets.getFirst().getId();
        Long remainingTicketId = boughtTickets.getLast().getId();

        // Refund only one of the two
        ticketService.refundTickets(List.of(refundedTicketId));

        Long groupId = orderRepository.findById(initialOrder.getId())
            .orElseThrow()
            .getOrderGroup()
            .getId();

        OrderGroup group = orderGroupRepository.findByIdWithOrders(groupId)
            .orElseThrow();

        assertAll(
            () -> assertEquals(3, group.getOrders().size(), "There should be three orders in the order group"),
            () -> {
                Order originalOrder = group.getOrders().stream()
                    .filter(o -> o.getOrderType() == OrderType.ORDER)
                    .findFirst()
                    .orElseThrow();

                Order refundOrder = group.getOrders().stream()
                    .filter(o -> o.getOrderType() == OrderType.REFUND)
                    .findFirst()
                    .orElseThrow();

                Order newOrder = group.getOrders().stream()
                    .filter(o -> o != originalOrder && o != refundOrder)
                    .findFirst()
                    .orElseThrow();

                assertEquals(2, originalOrder.getTickets().size(), "Original order should still contain both tickets");
                assertEquals(1, refundOrder.getTickets().size(), "Refund order should contain the refunded ticket");
                assertEquals(1, newOrder.getTickets().size(), "New order should contain the remaining ticket");

                Ticket refundedTicket = refundOrder.getTickets().getFirst();
                Ticket remainingTicket = newOrder.getTickets().getFirst();

                assertEquals(refundedTicketId, refundedTicket.getId(), "Refunded ticket ID should match");
                assertEquals(TicketStatus.REFUNDED, refundedTicket.getStatus(), "Refunded ticket should have status REFUNDED");

                assertEquals(remainingTicketId, remainingTicket.getId(), "Remaining ticket ID should match");
                assertEquals(TicketStatus.BOUGHT, remainingTicket.getStatus(), "Remaining ticket should still have status BOUGHT");
            }
        );
    }

    @Test
    @Transactional
    public void testBuyTickets_shouldThrowValidationException_whenInvalidCardDetails() {
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());

        TicketRequestDto request = createBuyRequest(List.of(target));
        request.setCardNumber("1234");
        request.setSecurityCode("12");
        request.setExpirationDate("12/31");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> ticketService.buyTickets(request),
            "Expected ValidationException due to invalid card data");

        assertTrue(ex.getMessage().toLowerCase().contains("card")
            || ex.getMessage().toLowerCase().contains("cvc"));
    }

    @Test
    @Transactional
    public void testBuyTickets_shouldThrowValidationException_whenInvalidAddressData() {
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());

        TicketRequestDto request = createBuyRequest(List.of(target));
        request.setStreet("");
        request.setCity(null);
        request.setPostalCode(null);
        request.setCountry("");

        ValidationException ex = assertThrows(ValidationException.class,
            () -> ticketService.buyTickets(request),
            "Expected ValidationException due to invalid address data");

        assertTrue(ex.getMessage().toLowerCase().contains("address")
                || ex.getMessage().toLowerCase().contains("street")
                || ex.getMessage().toLowerCase().contains("postal"),
            "Error message should mention invalid address");
    }

}



