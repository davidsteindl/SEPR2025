package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.*;
import at.ac.tuwien.sepr.groupphase.backend.exception.ReservationExpiredException;
import at.ac.tuwien.sepr.groupphase.backend.exception.SeatUnavailableException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class TicketServiceTest {

    @Autowired private TicketService ticketService;
    @Autowired private ShowService showService;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private EventLocationRepository eventLocationRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private ArtistRepository artistRepository;

    @MockitoBean
    private AuthenticationFacade authenticationFacade;

    private EventLocation location;
    private Room testRoom;
    private SeatedSector seatedSector;
    private Seat seat;
    private StandingSector standingSector;
    private Show testShow;

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

        // create and save an event and artist for the show
        Event testEvent = Event.EventBuilder.anEvent()
            .withName("Test Event")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDescription("Test description")
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

        // create and persist a show in the future
        Show show = Show.ShowBuilder.aShow()
            .withName("Test Show")
            .withDuration(60)
            .withDate(LocalDateTime.now().plusDays(1))
            .withEvent(testEvent)
            .withArtists(Set.of(testArtist))
            .withRoom(testRoom)
            .build();
        try {
            testShow = showService.createShow(show);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
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
        t1.setSectorId(seatedSector.getId()); t1.setSeatId(seat.getId());
        TicketTargetSeatedDto t2 = new TicketTargetSeatedDto();
        t2.setSectorId(seatedSector.getId()); t2.setSeatId(seat2.getId());
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
        assertThrows(SeatUnavailableException.class, () -> {
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
        Long reservedTicketId   = reservation.getTickets().getFirst().getId();

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
        assertThrows(ReservationExpiredException.class, () -> {
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
}
