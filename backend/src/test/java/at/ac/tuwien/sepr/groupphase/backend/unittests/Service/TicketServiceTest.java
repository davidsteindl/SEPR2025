package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.*;
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
}
