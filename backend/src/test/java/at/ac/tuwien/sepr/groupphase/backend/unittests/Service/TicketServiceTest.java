package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.*;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.OrderGroup;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
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
import org.junit.jupiter.api.function.Executable;
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
    public void testBuySingleTicket_createsTicketWithCorrectAttributes() {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());

        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        request.setTargets(List.of(target));

        OrderDto orderDto = ticketService.buyTickets(request);
        TicketDto dto = orderDto.getTickets().getFirst();


        assertAll(
            () -> assertNotNull(orderDto.getId()),
            () -> assertEquals(1, orderDto.getTickets().size()),
            () -> assertEquals(testShow.getName(), dto.getShowName()),
            () -> assertEquals(sector.getPrice(), dto.getPrice()),
            () -> assertEquals(seat.getId(), dto.getSeatId()),
            () -> assertEquals(sector.getId(), dto.getSectorId()),
            () -> assertEquals(TicketStatus.BOUGHT, dto.getStatus()),
            () -> assertEquals(1, orderRepository.findAll().size()),
            () -> assertEquals(1, ticketRepository.findAll().size())
        );

    }

    @Test
    @Transactional
    public void testBuyMultipleTickets_multipleSeated_createsMultipleTicketsAndSingleOrder() {
        TicketRequestDto request = new TicketRequestDto();
        request.setShowId(testShow.getId());

        List<TicketTargetSeatedDto> targets = testRoom.getSeats().stream()
            .filter(seat -> seat.getSector().equals(sector))
            .limit(3)
            .map(seat -> {
                TicketTargetSeatedDto dto = new TicketTargetSeatedDto();
                dto.setSectorId(seat.getSector().getId());
                dto.setSeatId(seat.getId());
                return dto;
            }).toList();

        request.setTargets(List.copyOf(targets));
        OrderDto orderDto = ticketService.buyTickets(request);

        assertAll(
            () -> assertNotNull(orderDto.getId()),
            () -> assertEquals(3, orderDto.getTickets().size()),
            () -> assertEquals(1, orderRepository.findAll().size()),
            () -> assertEquals(3, ticketRepository.findAll().size())
        );

        assertAll(
            orderDto.getTickets().stream()
                .map(dto -> (Executable) () -> assertAll(
                    () -> assertEquals(TicketStatus.BOUGHT, dto.getStatus()),
                    () -> assertEquals(sector.getId(), dto.getSectorId()),
                    () -> assertNotNull(dto.getSeatId())
                ))
                .toArray(Executable[]::new)
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
        buyTarget.setSectorId(sector.getId());
        buyTarget.setSeatId(seat.getId());
        buyReq.setTargets(List.of(buyTarget));
        ticketService.buyTickets(buyReq);

        // now try to reserve the same seat
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        TicketTargetSeatedDto resTarget = new TicketTargetSeatedDto();
        resTarget.setSectorId(sector.getId());
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
        buyTarget.setSectorId(sector.getId());
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
        resTarget.setSectorId(sector.getId());
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
        resTarget.setSectorId(sector.getId());
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
        target.setSectorId(sector.getId());
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
        buyTarget.setSectorId(sector.getId());
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
    public void testRefundTickets_updatesTicketStatusToRefunded() {
        // Buy first
        TicketRequestDto req = new TicketRequestDto();
        req.setShowId(testShow.getId());
        TicketTargetSeatedDto t = new TicketTargetSeatedDto();
        t.setSectorId(sector.getId());
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
        target.setSectorId(sector.getId());
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
        // Reserve a ticket
        TicketRequestDto reserveReq = new TicketRequestDto();
        reserveReq.setShowId(testShow.getId());
        TicketTargetSeatedDto target = new TicketTargetSeatedDto();
        target.setSectorId(sector.getId());
        target.setSeatId(seat.getId());
        reserveReq.setTargets(List.of(target));

        ReservationDto reservation = ticketService.reserveTickets(reserveReq);

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
        target.setSectorId(sector.getId());
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
        target.setSectorId(seatedSector.getId());
        target.setSeatId(seat.getId());
        OrderDto initialOrder = ticketService.buyTickets(createBuyRequest(List.of(target)));

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


}