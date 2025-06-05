package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatUsageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StageSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorUsageDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Hold;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HoldRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SeatRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SectorRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class RoomServiceTests {

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private HoldRepository holdRepository;

    @Autowired
    private SeatRepository seatRepository;

    // <-- let Spring inject the @Service (and its @Transactional proxy)
    @Autowired
    private RoomService roomService;

    private EventLocation testLocation;
    private CreateRoomDto createRoomDto;
    private Event event;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private SectorRepository sectorRepository;

    @BeforeEach
    public void setUp() {
        testLocation = EventLocation.EventLocationBuilder.anEventLocation()
            .withName("Test Venue")
            .withCountry("Austria")
            .withCity("Vienna")
            .withStreet("Sample Street")
            .withPostalCode("1010")
            .withType(EventLocation.LocationType.THEATER)
            .build();
        eventLocationRepository.save(testLocation);

        event = Event.EventBuilder.anEvent()
            .withName("Test Event")
            .withDescription("This is a test event")
            .withLocation(testLocation)
            .withCategory(Event.EventCategory.CLASSICAL)
            .withDuration(600)
            .withDateTime(LocalDateTime.now().plusYears(1))
            .build();
        event = eventRepository.save(event);

        createRoomDto = CreateRoomDto.CreateRoomDtoBuilder
            .aCreateRoomDtoBuilder()
            .eventLocationId(testLocation.getId())
            .name("Room A")
            .rows(10)
            .columns(10)
            .build();
    }

    @AfterEach
    public void tearDown() {
        ticketRepository.deleteAll();
        holdRepository.deleteAll();
        showRepository.deleteAll();
        eventRepository.deleteAll();
        roomRepository.deleteAll();
        eventLocationRepository.deleteAll();
        seatRepository.deleteAll();
        sectorRepository.deleteAll();
    }

    @Test
    public void testCreateRoom_validInput_createsRoom() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);
        assertAll(
            () -> assertNotNull(result.getId(), "Room ID should be generated"),
            () -> assertEquals("Room A", result.getName(), "Room name should match input"),
            () -> assertEquals(0, result.getSectors().size())
        );
    }

    @Test
    public void testCreateRoom_validInput_createsRoom_validId() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);
        assertNotNull(result.getId(), "Generated RoomDetailDto ID should not be null");
    }

    @Test
    public void testCreateRoom_validInput_createsRoom_allSectorsAreNormalSectors() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);
        for (SectorDto sector : result.getSectors()) {
            assertInstanceOf(SectorDto.class, sector,
                "All sectors should be SectorDto instances");
        }
    }


    @Test
    public void testUpdateRoom_nonExistingRoom_throws() {
        RoomDetailDto bogus = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(999L)
            .name("Doesn't Matter")
            .sectors(List.of())
            .build();

        assertThrows(EntityNotFoundException.class,
            () -> roomService.updateRoom(999L, bogus),
            "Updating a non-existing room ID should throw EntityNotFoundException");
    }

    @Test
    public void testUpdateRoom_removeSector_deletesIt() throws ValidationException {
        RoomDetailDto created = roomService.createRoom(createRoomDto);

        StandingSectorDto standing = new StandingSectorDto();
        standing.setPrice(10);
        standing.setCapacity(5);

        RoomDetailDto withSector = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(created.getId())
            .name(created.getName())
            .sectors(List.of(standing))
            .seats(created.getSeats())
            .eventLocationId(created.getEventLocationId())
            .build();

        RoomDetailDto updated = roomService.updateRoom(created.getId(), withSector);

        RoomDetailDto removed = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(updated.getId())
            .name(updated.getName())
            .sectors(List.of()) // remove all
            .seats(updated.getSeats())
            .eventLocationId(updated.getEventLocationId())
            .build();

        RoomDetailDto result = roomService.updateRoom(updated.getId(), removed);

        assertEquals(0, result.getSectors().size());
    }


    @Test
    public void testCreateRoom_validInput_createsRoom_RoomHasCorrectSeatCount() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);

        assertAll(
            () -> assertTrue(result.getSectors().isEmpty()),
            () -> assertEquals(100, result.getSeats().size())
        );
    }

    @Test
    public void testCreateRoom_validInput_seatsHaveCorrectRowAndColumnNumbers() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);

        assertAll(
            result.getSeats().stream().map(seat ->
                () -> assertAll(
                    () -> assertTrue(seat.getRowNumber() >= 1 && seat.getRowNumber() <= 10),
                    () -> assertTrue(seat.getColumnNumber() >= 1 && seat.getColumnNumber() <= 10)
                )
            )
        );
    }

    @Test
    public void testCreateRoom_eventLocationNotFound_throws() {
        CreateRoomDto invalid = CreateRoomDto.CreateRoomDtoBuilder.aCreateRoomDtoBuilder()
            .eventLocationId(9999L)
            .name("Invalid Room")
            .build();

        assertThrows(EntityNotFoundException.class, () -> roomService.createRoom(invalid));
    }

    @Test
    public void testUpdateRoom_addStandingSector() throws ValidationException {
        RoomDetailDto original = roomService.createRoom(createRoomDto);
        StandingSectorDto newSector = new StandingSectorDto();
        newSector.setPrice(55);
        newSector.setCapacity(45);

        RoomDetailDto update = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(original.getId())
            .name(original.getName())
            .sectors(new ArrayList<>(original.getSectors()) {{
                add(newSector);
            }})
            .seats(original.getSeats())
            .eventLocationId(original.getEventLocationId())
            .build();

        RoomDetailDto updated = roomService.updateRoom(original.getId(), update);

        assertAll(
            () -> assertEquals(1, updated.getSectors().size()),
            () -> assertTrue(updated.getSectors().stream()
                .anyMatch(s -> s instanceof StandingSectorDto && ((StandingSectorDto) s).getCapacity() == 45))
        );
    }

    @Test
    public void testUpdateRoom_addStageSector() throws ValidationException {
        RoomDetailDto original = roomService.createRoom(createRoomDto);
        StageSectorDto newSector = new StageSectorDto();

        RoomDetailDto update = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(original.getId())
            .name(original.getName())
            .sectors(new ArrayList<>(original.getSectors()) {{
                add(newSector);
            }})
            .seats(original.getSeats())
            .eventLocationId(original.getEventLocationId())
            .build();

        RoomDetailDto updated = roomService.updateRoom(original.getId(), update);

        assertAll(
            () -> assertEquals(1, updated.getSectors().size()),
            () -> assertTrue(updated.getSectors().stream()
                .anyMatch(s -> s instanceof StageSectorDto))
        );
    }

    @Test
    public void testUpdateRoom_addSectorWithNonExistingId_throwsEntityNotFoundException() {
        RoomDetailDto original = roomService.createRoom(createRoomDto);
        StandingSectorDto invalidSector = new StandingSectorDto();
        invalidSector.setId(9999L); // fake ID
        invalidSector.setCapacity(15);
        invalidSector.setPrice(30);

        RoomDetailDto update = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(original.getId())
            .name(original.getName())
            .sectors(List.of(invalidSector))
            .seats(original.getSeats())
            .eventLocationId(original.getEventLocationId())
            .build();

        assertThrows(ValidationException.class, () -> roomService.updateRoom(original.getId(), update));
    }

    @Test
    public void testGetAllRooms_returnsAllPersistedRooms() {
        roomService.createRoom(createRoomDto);
        roomService.createRoom(createRoomDto);

        List<RoomDetailDto> allRooms = roomService.getAllRooms();
        assertEquals(2, allRooms.size());
    }

    @Test
    public void testGetRoomUsageForShow_allSeatsAvailable_noTickets_noHolds() {
        RoomDetailDto room = roomService.createRoom(createRoomDto);

        Sector sector = new Sector();
        sector.setRoom(roomRepository.findById(room.getId()).orElseThrow());
        sector.setPrice(20);
        sectorRepository.save(sector);

        List<Seat> allSeats = seatRepository.findAll();
        for (Seat seat : allSeats) {
            seat.setSector(sector);
        }
        seatRepository.saveAll(allSeats);

        Show show = Show.ShowBuilder.aShow()
            .withName("Test Show")
            .withDate(LocalDateTime.now().plusYears(1))
            .withDuration(120)
            .withRoom(roomRepository.findById(room.getId()).orElseThrow())
            .withEvent(event)
            .build();

        show = showRepository.save(show);

        RoomDetailDto usage = roomService.getRoomUsageForShow(show.getId());

        assertAll(
            () -> assertEquals(100, usage.getSeats().size(), "All seats should be returned"),
            () -> assertTrue(usage.getSeats().stream()
                .map(s -> (SeatUsageDto) s)
                .allMatch(SeatUsageDto::isAvailable), "All seats should be available"),
            () -> assertEquals(1, usage.getSectors().size(), "One sector should be present")
        );
    }


    @Test
    public void testGetRoomUsageForShow_someSeatsOccupied_byTickets() {
        RoomDetailDto room = roomService.createRoom(createRoomDto);

        Sector sector = new Sector();
        sector.setRoom(roomRepository.findById(room.getId()).orElseThrow());
        sector.setPrice(20);
        sector = sectorRepository.save(sector);

        List<Seat> allSeats = seatRepository.findAll();

        for (Seat seat : allSeats) {
            seat.setSector(sector);
        }
        seatRepository.saveAll(allSeats);


        Show show = Show.ShowBuilder.aShow()
            .withName("Test Show")
            .withDate(LocalDateTime.now().plusYears(1))
            .withDuration(120)
            .withRoom(roomRepository.findById(room.getId()).orElseThrow())
            .withEvent(event)
            .build();

        show = showRepository.save(show);

        List<Seat> targetedSeats = allSeats.subList(0, 3);
        for (Seat seat : targetedSeats) {
            Ticket ticket = new Ticket();
            ticket.setShow(show);
            ticket.setStatus(TicketStatus.BOUGHT);
            ticket.setSeat(seat);
            ticket.setSector(seat.getSector());
            ticket.setCreatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }

        RoomDetailDto usage = roomService.getRoomUsageForShow(show.getId());

        long unavailableCount = usage.getSeats().stream()
            .map(s -> (SeatUsageDto) s)
            .filter(s -> !s.isAvailable())
            .count();

        assertEquals(3, unavailableCount, "Exactly 3 seats should be marked unavailable");
    }

    @Test
    @Transactional
    public void testGetRoomUsageForShow_standingSector_soldAndHeld() throws ValidationException {
        RoomDetailDto room = roomService.createRoom(createRoomDto);
        StandingSectorDto standing = new StandingSectorDto();
        standing.setPrice(15);
        standing.setCapacity(10);

        room = roomService.updateRoom(room.getId(), RoomDetailDto.RoomDetailDtoBuilder
            .aRoomDetailDto()
            .id(room.getId())
            .name(room.getName())
            .eventLocationId(room.getEventLocationId())
            .seats(room.getSeats())
            .sectors(List.of(standing))
            .build());

        StandingSector sectorEntity = (StandingSector) roomRepository.findById(room.getId()).get()
            .getSectors().stream().findFirst().orElseThrow();

        Show show = Show.ShowBuilder.aShow()
            .withName("Test Show")
            .withDate(LocalDateTime.now().plusYears(1))
            .withDuration(120)
            .withRoom(roomRepository.findById(room.getId()).orElseThrow())
            .withEvent(event)
            .build();

        show = showRepository.save(show);

        for (int i = 0; i < 3; i++) {
            Ticket ticket = new Ticket();
            ticket.setShow(show);
            ticket.setStatus(TicketStatus.BOUGHT);
            ticket.setSector(sectorEntity);
            ticketRepository.save(ticket);
        }

        for (int i = 0; i < 2; i++) {
            Hold hold = new Hold();
            hold.setShowId(show.getId());
            hold.setUserId(1L);
            hold.setSectorId(sectorEntity.getId());
            hold.setValidUntil(LocalDateTime.now().plusMinutes(10));
            holdRepository.save(hold);
        }

        RoomDetailDto usage = roomService.getRoomUsageForShow(show.getId());

        StandingSectorUsageDto usageDto = (StandingSectorUsageDto) usage.getSectors().stream()
            .filter(s -> s instanceof StandingSectorUsageDto)
            .findFirst().orElseThrow();

        assertEquals(5, usageDto.getAvailableCapacity(), "Should be 5 available from 10");
    }

}
