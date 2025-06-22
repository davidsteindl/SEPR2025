package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.*;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.OrderGroup;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ReservationExpiredException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ReservationNotFoundException;
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
import java.util.ArrayList;
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
    @Autowired
    private HoldRepository holdRepository;

    @MockitoBean
    private AuthenticationFacade authenticationFacade;

    private EventLocation location;
    private Room testRoom;
    private Sector sector;
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
        testRoom = Room.RoomBuilder.aRoom()
            .withName("Test Room")
            .withEventLocation(location)
            .withSeats(new ArrayList<>())
            .build();

        sector = Sector.SectorBuilder.aSector()
            .withPrice(100)
            .withRoom(testRoom)
            .build();
        testRoom.addSector(sector);

        standingSector = StandingSector.StandingSectorBuilder.aStandingSector()
            .withPrice(50)
            .withCapacity(10)
            .withRoom(testRoom)
            .build();
        testRoom.addSector(standingSector);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 7; j++) {
                Seat s = new Seat();
                s.setRowNumber(i + 1);
                s.setColumnNumber(j + 1);
                s.setDeleted(false);
                if (i < 2) {
                    s.setSector(sector);
                } else {
                    s.setSector(standingSector);
                }
                testRoom.addSeat(s);

                if (seat == null && s.getSector().equals(sector)) {
                    seat = s;
                }
            }
        }

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
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        TicketRequestDto request = createBuyRequest(List.of(target));

        OrderGroupDto orderGroupDto = ticketService.buyTickets(request);

        OrderDto orderDto = orderGroupDto.getOrders().getFirst();
        TicketDto dto = orderDto.getTickets().getFirst();

        var orderEntity = orderRepository.findById(orderDto.getId()).orElseThrow();

        assertAll(
            () -> assertNotNull(orderDto.getId()),
            () -> assertEquals(1, orderDto.getTickets().size()),
            () -> assertEquals(testShow.getName(), dto.getShowName()),
            () -> assertEquals(sector.getPrice(), dto.getPrice()),
            () -> assertEquals(seat.getId(), dto.getSeatId()),
            () -> assertEquals(sector.getId(), dto.getSectorId()),
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
            () -> assertEquals(1, ticketRepository.findAll().size()),
            () -> assertEquals(orderEntity.getOrderGroup().getId(), orderGroupDto.getId()),
            () -> assertEquals(testShow.getName(), orderGroupDto.getShowName())
        );
    }

    @Test
    @Transactional
    public void testBuyStandingTicket_savesAddressAndGroupCorrectly() throws ValidationException {
        TicketTargetStandingDto standingTarget = new TicketTargetStandingDto();
        standingTarget.setSectorId(standingSector.getId());
        standingTarget.setQuantity(2);
        TicketRequestDto request = createBuyRequest(List.of(standingTarget));

        OrderGroupDto orderGroupDto = ticketService.buyTickets(request);

        OrderDto orderDto = orderGroupDto.getOrders().getFirst();
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
            () -> assertEquals(1L, orderEntity.getOrderGroup().getUserId()),
        () -> assertEquals(orderEntity.getOrderGroup().getId(), orderGroupDto.getId())
        );
    }

    @Test
    @Transactional
    public void testBuyMultipleTickets_multipleSeated_createsMultipleTicketsAndSingleOrder() throws ValidationException {
        // Suche zwei verschiedene Seats aus dem bestehenden Sector
        List<Seat> seatsInSector = testRoom.getSeats().stream()
            .filter(s -> s.getSector() != null && s.getSector().equals(sector))
            .limit(2)
            .toList();

        assertEquals(2, seatsInSector.size(), "Es müssen mindestens zwei Seats im selben Sector vorhanden sein");

        Seat first = seatsInSector.get(0);
        Seat second = seatsInSector.get(1);

        // Erstelle Ticket-Ziele
        TicketTargetSeatedDto t1 = new TicketTargetSeatedDto();
        t1.setSectorId(sector.getId());
        t1.setSeatId(first.getId());

        TicketTargetSeatedDto t2 = new TicketTargetSeatedDto();
        t2.setSectorId(sector.getId());
        t2.setSeatId(second.getId());

        TicketRequestDto request = createBuyRequest(List.of(t1, t2));

        OrderGroupDto orderGroupDto = ticketService.buyTickets(request);
        OrderDto orderDto = orderGroupDto.getOrders().getFirst();

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

        OrderGroupDto orderGroupDto = ticketService.buyTickets(request);
        OrderDto orderDto = orderGroupDto.getOrders().getFirst();

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
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        request.setTargets(List.of(target));

        ReservationDto reservationDto = ticketService.reserveTickets(request);

        assertNotNull(reservationDto.getId());
        assertEquals(1, reservationDto.getTickets().size());
        TicketDto dto = reservationDto.getTickets().getFirst();
        assertEquals(TicketStatus.RESERVED, dto.getStatus());
        assertEquals(OrderType.RESERVATION, reservationDto.getOrderType());
        assertEquals(testShow.getDate().minusMinutes(30), reservationDto.getExpiresAt());

        Order order = orderRepository.findById(reservationDto.getId()).orElseThrow();
        assertEquals(order.getOrderGroup().getId(), reservationDto.getGroupId(),
            "ReservationDto should contain the OrderGroup ID");

        assertEquals(1, orderRepository.findAll().size());
        assertEquals(1, ticketRepository.findAll().size());
    }

    @Test
    @Transactional
    public void testReserveOnAlreadyBoughtTicket_throwsValidationException() throws ValidationException {
        // buy a ticket first
        TicketTargetSeatedDto buyTarget = new TicketTargetSeatedDto();
        buyTarget.setSectorId(sector.getId());
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
    public void testBuyReservedTickets_withCheckoutData_createsOrderAndSetsAddress() throws ValidationException {
        // 1. Reserve a seat
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());

        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        reserveReq.setTargets(List.of(target));
        ReservationDto reservation = ticketService.reserveTickets(reserveReq);

        Long reservedTicketId = reservation.getTickets().getFirst().getId();
        assertEquals(TicketStatus.RESERVED, reservation.getTickets().getFirst().getStatus());

        // 2. Prepare buy request using the reserved ticket
        TicketRequestDto buyReq = createBuyRequest(List.of(target));
        buyReq.setReservedTicketIds(List.of(reservedTicketId));

        // 3. Buy reserved ticket
        OrderGroupDto groupResult = ticketService.buyReservedTickets(buyReq);
        OrderDto result = groupResult.getOrders().getFirst();

        TicketDto boughtTicket = result.getTickets().getFirst();
        var order = orderRepository.findById(result.getId()).orElseThrow();

        assertAll(
            () -> assertNotNull(result.getId(), "Order ID should not be null"),
            () -> assertEquals(1, result.getTickets().size(), "Should contain one ticket"),
            () -> assertEquals(reservedTicketId, boughtTicket.getId(), "Ticket ID should match"),
            () -> assertEquals(TicketStatus.BOUGHT, boughtTicket.getStatus(), "Ticket status should be BOUGHT"),
            () -> assertEquals(firstName, order.getFirstName(), "First name should be stored in order"),
            () -> assertEquals(lastName, order.getLastName(), "Last name should be stored in order"),
            () -> assertEquals(street, order.getStreet(), "Street should be stored"),
            () -> assertEquals(houseNumber, order.getHousenumber(), "House number should be stored"),
            () -> assertEquals(city, order.getCity(), "City should be stored"),
            () -> assertEquals(country, order.getCountry(), "Country should be stored"),
            () -> assertEquals(postalCode, order.getPostalCode(), "Postal code should be stored"),
            () -> assertEquals(OrderType.ORDER, result.getOrderType(), "Order type should be ORDER")
        );
    }

    @Test
    @Transactional
    public void testBuyReservedTickets_onAlreadyBoughtTicket_throwsValidationException() throws ValidationException {
        // Buy ticket normally
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());

        TicketRequestDto buyReq = createBuyRequest(List.of(target));
        OrderGroupDto boughtGroup = ticketService.buyTickets(buyReq);

        Long boughtTicketId = boughtGroup.getOrders().getFirst().getTickets().getFirst().getId();

        // Try to buy again as "reserved"
        TicketRequestDto invalidBuyReservedReq = createBuyRequest(List.of(target));
        invalidBuyReservedReq.setReservedTicketIds(List.of(boughtTicketId));

        assertThrows(IllegalArgumentException.class, () -> {
            ticketService.buyReservedTickets(invalidBuyReservedReq);
        });
    }


    @Test
    @Transactional
    public void testBuyReservedTickets_convertsReservationToNewOrder() throws ValidationException {
        // Reserve ticket
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());

        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        reserveReq.setTargets(List.of(target));
        ReservationDto reservation = ticketService.reserveTickets(reserveReq);

        Long reservedTicketId = reservation.getTickets().getFirst().getId();
        Long oldOrderId = reservation.getId();

        // Buy reserved ticket
        TicketRequestDto buyReq = createBuyRequest(List.of(target));
        buyReq.setReservedTicketIds(List.of(reservedTicketId));

        OrderGroupDto groupResult = ticketService.buyReservedTickets(buyReq);
        OrderDto newOrder = groupResult.getOrders().getFirst();

        var oldOrder = orderRepository.findById(oldOrderId).orElseThrow();
        var newOrderEntity = orderRepository.findById(newOrder.getId()).orElseThrow();

        assertAll(
            () -> assertEquals(OrderType.ORDER, newOrder.getOrderType()),
            () -> assertEquals(1, newOrder.getTickets().size()),
            () -> assertEquals(reservedTicketId, newOrder.getTickets().getFirst().getId()),
            () -> assertEquals(TicketStatus.BOUGHT, newOrder.getTickets().getFirst().getStatus()),
            () -> assertTrue(oldOrder.getTickets().isEmpty(), "Old reservation should no longer hold the ticket")
        );
    }



    @Test
    @Transactional
    public void testBuyReservedTickets_onExpiredReservation_throwsReservationExpiredException() {
        // Reserve ticket
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());

        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        reserveReq.setTargets(List.of(target));
        ReservationDto reservation = ticketService.reserveTickets(reserveReq);

        Long reservedTicketId = reservation.getTickets().getFirst().getId();

        // Simulate expiry
        Ticket reserved = ticketRepository.findById(reservedTicketId).orElseThrow();
        reserved.setShow(testShow);
        reserved.setStatus(TicketStatus.RESERVED);
        reserved.getShow().setDate(LocalDateTime.now().plusMinutes(29)); // Now it's <30 min left
        ticketRepository.save(reserved);

        // Try to buy expired reservation
        TicketRequestDto buyReq = createBuyRequest(List.of(target));
        buyReq.setReservedTicketIds(List.of(reservedTicketId));

        assertThrows(ReservationExpiredException.class, () -> {
            ticketService.buyReservedTickets(buyReq);
        });
    }


    @Test
    @Transactional
    public void testCancelReservations_releasesSeat_forNewReservation() {
        // Reserve a seat
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
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
        buyTarget.setSectorId(sector.getId());
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

        OrderGroupDto buyGroup = ticketService.buyTickets(buyReq);
        OrderDto buyOrder = buyGroup.getOrders().getFirst();
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
        OrderGroupDto rebuyGroup = ticketService.buyTickets(buyReq);
        OrderDto rebuy = rebuyGroup.getOrders().getFirst();
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
        t.setSectorId(sector.getId());
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
        t.setSectorId(sector.getId());
        t.setSeatId(seat.getId());

        TicketRequestDto req = createBuyRequest(List.of(t));
        OrderGroupDto buyGroup = ticketService.buyTickets(req);
        OrderDto ord = buyGroup.getOrders().getFirst();

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
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        TicketRequestDto request = createBuyRequest(List.of(target));
        OrderGroupDto initialGroup = ticketService.buyTickets(request);
        OrderDto initialOrder = initialGroup.getOrders().getFirst();
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


    @Test
    @Transactional
    public void testPartialRefund_createsThreeOrdersInOrderGroup() throws ValidationException {
        // Zwei Seats aus demselben Sector wählen
        List<Seat> seatsInSector = testRoom.getSeats().stream()
            .filter(s -> s.getSector() != null && s.getSector().equals(sector))
            .limit(2)
            .toList();

        assertEquals(2, seatsInSector.size(), "Es müssen mindestens zwei Seats im selben Sector vorhanden sein");

        Seat first = seatsInSector.get(0);
        Seat second = seatsInSector.get(1);

        // Ticket-Ziele erstellen
        TicketTargetSeatedDto t1 = new TicketTargetSeatedDto();
        t1.setSectorId(sector.getId());
        t1.setSeatId(first.getId());

        TicketTargetSeatedDto t2 = new TicketTargetSeatedDto();
        t2.setSectorId(sector.getId());
        t2.setSeatId(second.getId());

        TicketRequestDto request = createBuyRequest(List.of(t1, t2));
        OrderGroupDto initialGroup = ticketService.buyTickets(request);
        OrderDto initialOrder = initialGroup.getOrders().getFirst();
        List<TicketDto> boughtTickets = initialOrder.getTickets();

        Long refundedTicketId = boughtTickets.getFirst().getId();
        Long remainingTicketId = boughtTickets.getLast().getId();

        // Refund nur eines der beiden Tickets
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
        target.setSectorId(sector.getId());
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
        target.setSectorId(sector.getId());
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

    @Test
    @Transactional
    public void testGetOrderGroupsByCategory_returnsOnlyPurchasedFutureGroups() throws ValidationException {
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(standingSector.getId());
        target.setQuantity(1);
        TicketRequestDto request = createBuyRequest(List.of(target));
        ticketService.buyTickets(request);

        var page = ticketService.getOrderGroupsByCategory(false, false, Pageable.ofSize(10));

        assertAll(
            () -> assertNotNull(page),
            () -> assertEquals(1, page.getTotalElements(), "Should return one order group"),
            () -> assertEquals("Test Show", page.getContent().getFirst().getShowName()),
            () -> assertEquals(location.getName(), page.getContent().getFirst().getLocationName())
        );
    }

    @Test
    @Transactional
    public void testGetOrderGroupsByCategory_whenNoReservationsExist_returnsEmptyPage() throws ValidationException {
        TicketTargetStandingDto target = new TicketTargetStandingDto();
        target.setSectorId(standingSector.getId());
        target.setQuantity(1);
        TicketRequestDto request = createBuyRequest(List.of(target));
        ticketService.buyTickets(request);

        var page = ticketService.getOrderGroupsByCategory(true, false, Pageable.ofSize(10));

        assertAll(
            () -> assertNotNull(page),
            () -> assertEquals(0, page.getTotalElements(), "Expected no reservation order groups"),
            () -> assertTrue(page.getContent().isEmpty(), "Result list should be empty")
        );
    }

    @Test
    @Transactional
    public void getOrderGroupDetails_shouldReturnCorrectInfo_whenGroupExistsAndBelongsToUser() throws ValidationException {
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        OrderGroupDto initialGroup = ticketService.buyTickets(createBuyRequest(List.of(target)));
        OrderDto initialOrder = initialGroup.getOrders().getFirst();

        Long groupId = orderRepository.findById(initialOrder.getId())
            .orElseThrow()
            .getOrderGroup()
            .getId();

        OrderGroupDetailDto dto = ticketService.getOrderGroupDetails(groupId);

        assertAll(
            () -> assertNotNull(dto, "DTO should not be null"),
            () -> assertEquals(groupId, dto.getId(), "Group ID should match"),
            () -> assertEquals("Test Show", dto.getShowName(), "Show name should match"),
            () -> assertEquals(location.getName(), dto.getLocationName(), "Location name should match"),
            () -> assertEquals(testShow.getDate(), dto.getShowDate(), "Show date should match"),

            () -> assertEquals(1, dto.getTickets().size(), "Should contain 1 ticket"),
            () -> assertEquals(TicketStatus.BOUGHT, dto.getTickets().getFirst().getStatus(), "Ticket should be BOUGHT"),
            () -> assertEquals(1, dto.getOrders().size(), "Should contain 1 order"),
            () -> assertEquals(OrderType.ORDER, dto.getOrders().getFirst().getOrderType(), "Order type should be ORDER")
        );
    }

    @Test
    @Transactional
    public void getOrderGroupDetails_shouldThrowNotFound_whenGroupDoesNotExistOrWrongUser() {
        Long nonExistentGroupId = 999999L;

        assertThrows(NotFoundException.class, () -> {
            ticketService.getOrderGroupDetails(nonExistentGroupId);
        });
    }


    @Test
    public void testLoadShow_whenShowNotFound_shouldThrowNotFoundException() {
        Long fakeShowId = 99999L;
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(fakeShowId);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> {
            ticketService.reserveTickets(request);
        });

        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    public void testCreateTickets_withUnknownTicketTarget_shouldThrowIllegalArgumentException() {
        var dummyTarget = new TicketTargetDto() {}; // Anonyme Klasse
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());
        request.setTargets(List.of(dummyTarget));
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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ticketService.buyTickets(request);
        });

        assertTrue(ex.getMessage().contains("Unknown ticket target"));
    }

    @Test
    @Transactional
    public void testProcessTicketTransfer_withOrderWithoutAddress() {
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        reserveReq.setTargets(List.of(target));
        ReservationDto reservation = ticketService.reserveTickets(reserveReq);

        Long reservedTicketId = reservation.getTickets().getFirst().getId();

        List<TicketDto> refunded = ticketService.cancelReservations(List.of(reservedTicketId));

        assertEquals(1, refunded.size());
        assertEquals(TicketStatus.CANCELLED, refunded.getFirst().getStatus());
    }

    @Test
    @Transactional
    public void testGetOrderById_shouldReturnCorrectOrder() throws ValidationException {
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        TicketRequestDto request = createBuyRequest(List.of(target));
        OrderGroupDto group = ticketService.buyTickets(request);
        OrderDto createdOrder = group.getOrders().getFirst();

        OrderDto fetched = ticketService.getOrderById(createdOrder.getId());

        assertNotNull(fetched);
        assertEquals(createdOrder.getId(), fetched.getId());
        assertEquals(1, fetched.getTickets().size());
    }

    @Test
    @Transactional
    public void testGetOrderById_shouldReturnCorrectOrder_withAssertAll() throws ValidationException {
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        TicketRequestDto request = createBuyRequest(List.of(target));
        OrderGroupDto group = ticketService.buyTickets(request);
        OrderDto createdOrder = group.getOrders().getFirst();

        OrderDto fetched = ticketService.getOrderById(createdOrder.getId());

        assertAll(
            () -> assertNotNull(fetched, "Fetched Order should not be null"),
            () -> assertEquals(createdOrder.getId(), fetched.getId(), "Fetched Order ID should match"),
            () -> assertEquals(1, fetched.getTickets().size(), "Fetched Order should contain 1 ticket"),
            () -> assertEquals(createdOrder.getTickets().getFirst().getId(), fetched.getTickets().getFirst().getId(), "Ticket ID should match")
        );
    }

    @Test
    @Transactional
    public void testGetTicketById_shouldReturnCorrectTicket() throws ValidationException {
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        TicketRequestDto request = createBuyRequest(List.of(target));
        OrderGroupDto group = ticketService.buyTickets(request);
        TicketDto createdTicket = group.getOrders().getFirst().getTickets().getFirst();

        TicketDto fetched = ticketService.getTicketById(createdTicket.getId());

        assertAll(
            () -> assertNotNull(fetched, "Fetched Ticket should not be null"),
            () -> assertEquals(createdTicket.getId(), fetched.getId(), "Ticket ID should match"),
            () -> assertEquals(seat.getId(), fetched.getSeatId(), "Seat ID should match"),
            () -> assertEquals(sector.getId(), fetched.getSectorId(), "Sector ID should match"),
            () -> assertEquals(TicketStatus.BOUGHT, fetched.getStatus(), "Ticket status should be BOUGHT")
        );
    }

    @Test
    @Transactional
    public void testCreateTicketHold_shouldPersistHoldWithCorrectValues() {
        CreateHoldDto dto = new CreateHoldDto();
        dto.setShowId(testShow.getId());
        dto.setSectorId(sector.getId());
        dto.setSeatId(seat.getId());

        ticketService.createTicketHold(dto);

        List<Hold> holds = holdRepository.findAll();
        assertAll(
            () -> assertEquals(1, holds.size(), "One hold should be created"),
            () -> assertEquals(dto.getShowId(), holds.getFirst().getShowId(), "Show ID should match"),
            () -> assertEquals(dto.getSectorId(), holds.getFirst().getSectorId(), "Sector ID should match"),
            () -> assertEquals(dto.getSeatId(), holds.getFirst().getSeatId(), "Seat ID should match"),
            () -> assertEquals(1L, holds.getFirst().getUserId(), "User ID should match stubbed user"),
            () -> assertTrue(holds.getFirst().getValidUntil().isAfter(LocalDateTime.now()), "Valid until should be in the future")
        );
    }

    @Test
    public void testReserveTickets_whenShowDoesNotExist_shouldThrowNotFoundException() {
        Long nonExistentShowId = 999999L;

        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(nonExistentShowId);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> {
            ticketService.reserveTickets(request);
        });

        assertTrue(ex.getMessage().contains("not found"));
    }


    @Test
    @Transactional
    public void testCancelReservations_whenNoTicketsFound_shouldThrowReservationNotFoundException() {
        List<Long> nonExistentIds = List.of(999999L, 888888L);

        assertThrows(ReservationNotFoundException.class, () -> {
            ticketService.cancelReservations(nonExistentIds);
        });
    }

    @Test
    @Transactional
    public void testRefundTickets_whenNoTicketsFound_shouldThrowReservationNotFoundException() {
        List<Long> nonExistentIds = List.of(999999L, 888888L);

        assertThrows(ReservationNotFoundException.class, () -> {
            ticketService.refundTickets(nonExistentIds);
        });
    }

}