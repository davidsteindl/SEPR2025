package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ReservationCleanUpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ReservationCleanUpServiceTest {

    @Autowired
    private ReservationCleanUpService cleanUpService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Show testShow;

    @BeforeEach
    @Transactional
    public void setUp() throws Exception {
        EventLocation location = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Test Location")
            .withCity("Vienna")
            .withCountry("Austria")
            .withPostalCode("1010")
            .withStreet("Main Street")
            .withType(EventLocation.LocationType.HALL)
            .build();
        eventLocationRepository.save(location);

        Room room = Room.RoomBuilder.aRoom()
            .withName("Test Room")
            .withEventLocation(location)
            .build();

        Sector sector = Sector.SectorBuilder.aSector()
            .withPrice(100)
            .withName("Test Sector")
            .withRoom(room)
            .build();
        room.addSector(sector);

        Seat seat = new Seat();
        seat.setRowNumber(1);
        seat.setColumnNumber(1);
        seat.setSector(sector);
        room.addSeat(seat);

        StandingSector standingSector = StandingSector.StandingSectorBuilder.aStandingSector()
            .withPrice(50)
            .withName("Standing Area")
            .withCapacity(100)
            .withRoom(room)
            .build();
        room.addSector(standingSector);

        roomRepository.save(room);

        Event event = Event.EventBuilder.anEvent()
            .withName("Cleanup Event")
            .withDescription("This is a test event for cleanup")
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDateTime(LocalDateTime.now().plusHours(1))
            .withDuration(120)
            .withLocation(location)
            .build();
        eventRepository.save(event);

        testShow = Show.ShowBuilder.aShow()
            .withName("Cleanup Show")
            .withDate(LocalDateTime.now().plusMinutes(20))
            .withDuration(60)
            .withEvent(event)
            .withRoom(room)
            .build();
        showRepository.save(testShow);

        Ticket seatedTicket = new Ticket();
        seatedTicket.setShow(testShow);
        seatedTicket.setSeat(seat);
        seatedTicket.setSector(seat.getSector());
        seatedTicket.setStatus(TicketStatus.RESERVED);
        ticketRepository.save(seatedTicket);
    }


    @Test
    @Transactional
    public void testCleanupServiceCancelsExpiredReservations() {
        List<Ticket> reservedBefore = ticketRepository.findByShowAndStatus(testShow, TicketStatus.RESERVED);
        assertEquals(1, reservedBefore.size(), "There should be one RESERVED ticket before cleanup");

        cleanUpService.cancelExpiredReservations();

        List<Ticket> reservedAfter = ticketRepository.findByShowAndStatus(testShow, TicketStatus.RESERVED);
        List<Ticket> cancelledAfter = ticketRepository.findByShowAndStatus(testShow, TicketStatus.EXPIRED);

        assertTrue(reservedAfter.isEmpty(), "No RESERVED tickets should remain after cleanup");
        assertEquals(1, cancelledAfter.size(), "One ticket should be CANCELLED after cleanup");
    }
}
