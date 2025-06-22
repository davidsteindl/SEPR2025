package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomPageDto;
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
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            () -> assertEquals(1, result.getSectors().size())
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

        assertThrows(NotFoundException.class,
            () -> roomService.updateRoom(999L, bogus),
            "Updating a non-existing room ID should throw NotFoundException");
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
            .seats(List.of())
            .eventLocationId(created.getEventLocationId())
            .build();

        RoomDetailDto updated = roomService.updateRoom(created.getId(), withSector);

        RoomDetailDto removed = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(updated.getId())
            .name(updated.getName())
            .sectors(List.of()) // remove all
            .seats(List.of())
            .eventLocationId(updated.getEventLocationId())
            .build();

        RoomDetailDto result = roomService.updateRoom(updated.getId(), removed);

        assertEquals(0, result.getSectors().size());
    }


    @Test
    public void testCreateRoom_validInput_createsRoom_RoomHasCorrectSeatCount() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);

        assertAll(
            () -> assertEquals(1, result.getSectors().size(), "Default sector should be present"),
            () -> assertEquals(100, result.getSeats().size(), "Expected 10x10 seats")
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

        assertThrows(NotFoundException.class, () -> roomService.createRoom(invalid));
    }

    @Test
    public void testUpdateRoom_addSectorWithNonExistingId_throwsNotFoundException() {
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
    public void testGetAllRoomsPaginated_returnsPagedRoomDetailDtos() {

        RoomDetailDto first = roomService.createRoom(createRoomDto);

        CreateRoomDto secondDto = CreateRoomDto.CreateRoomDtoBuilder
            .aCreateRoomDtoBuilder()
            .eventLocationId(testLocation.getId())
            .name("Room B")
            .rows(5)
            .columns(5)
            .build();

        RoomDetailDto second = roomService.createRoom(secondDto);

        Pageable pageable = PageRequest.of(0, 2);
        Page<RoomPageDto> page = roomService.getAllRoomsPaginated(pageable);

        assertAll(
            () -> assertEquals(2, page.getTotalElements(), "There should be 2 rooms in total contained"),
            () -> assertEquals(2, page.getSize(), " Page size should be 2"),
            () -> assertEquals(2, page.getContent().size(), "Page should contain 2 rooms"),
            () -> assertEquals(0, page.getNumber(), " Page number should be 0"),
            () -> assertEquals(1, page.getTotalPages(), "Total pages should be 1"),
            () -> assertTrue(
                page.getContent().stream().anyMatch(r -> r.getName().equals("Room A")),
                "Page should contain Room A"
            ),
            () -> assertTrue(
                page.getContent().stream().anyMatch(r -> r.getName().equals("Room B")),
                "Page should contain Room B"
            )
        );
    }

    @Test
    public void testGetAllRoomsPaginated_empty_returnsEmptyPage() {
        roomRepository.deleteAll();

        Pageable pageable = PageRequest.of(0, 1);
        Page<RoomPageDto> page = roomService.getAllRoomsPaginated(pageable);

        assertAll(
            () -> assertEquals(0, page.getTotalElements(), "No rooms present, total should be 0"),
            () -> assertTrue(page.getContent().isEmpty(), "Content page should be empty")
        );
    }

    @Test
    public void testGetRoomUsageForShow_allSeatsAvailable_noTickets_noHolds() {
        RoomDetailDto room = roomService.createRoom(createRoomDto);

        // get default sector
        Sector existingSector = sectorRepository.findAll().stream()
            .filter(s -> s.getRoom().getId().equals(room.getId()))
            .findFirst()
            .orElseThrow();

        // assign seats
        List<Seat> allSeats = seatRepository.findAll();
        for (Seat seat : allSeats) {
            seat.setSector(existingSector);
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

        // get default sector
        Sector sector = sectorRepository.findAll().stream()
            .filter(s -> s.getRoom().getId().equals(room.getId()))
            .findFirst()
            .orElseThrow();

        List<Seat> allSeats = seatRepository.findAll();

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
    public void testUpdateRoom_addSectorAndReassignSomeSeats() throws ValidationException {
        RoomDetailDto room = roomService.createRoom(createRoomDto);

        SectorDto defaultSector = room.getSectors().get(0);

        SectorDto newSector = new SectorDto();
        newSector.setPrice(50);

        RoomDetailDto withNewSector = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(room.getId())
            .name(room.getName())
            .eventLocationId(room.getEventLocationId())
            .sectors(List.of(defaultSector, newSector))
            .seats(room.getSeats())
            .build();

        RoomDetailDto updatedRoom = roomService.updateRoom(room.getId(), withNewSector);

        assertEquals(2, updatedRoom.getSectors().size());

        SectorDto createdNewSector = updatedRoom.getSectors().stream()
            .filter(s -> !Objects.equals(s.getId(), defaultSector.getId()))
            .findFirst().orElseThrow();

        List<SeatDto> reAssignedSeats = new ArrayList<>();
        for (int i = 0; i < updatedRoom.getSeats().size(); i++) {
            SeatDto seat = updatedRoom.getSeats().get(i);
            SeatDto updated = new SeatDto();
            updated.setId(seat.getId());
            updated.setRowNumber(seat.getRowNumber());
            updated.setColumnNumber(seat.getColumnNumber());
            updated.setDeleted(seat.isDeleted());
            if (i < updatedRoom.getSeats().size() / 2) {
                updated.setSectorId(createdNewSector.getId());
            } else {
                updated.setSectorId(defaultSector.getId());
            }
            reAssignedSeats.add(updated);
        }

        RoomDetailDto reassignedRoom = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(updatedRoom.getId())
            .name(updatedRoom.getName())
            .eventLocationId(updatedRoom.getEventLocationId())
            .sectors(List.of(defaultSector, createdNewSector))
            .seats(reAssignedSeats)
            .build();

        RoomDetailDto result = roomService.updateRoom(updatedRoom.getId(), reassignedRoom);

        long countNewSector = result.getSeats().stream().filter(s -> s.getSectorId().equals(createdNewSector.getId())).count();
        long countDefaultSector = result.getSeats().stream().filter(s -> s.getSectorId().equals(defaultSector.getId())).count();

        assertEquals(result.getSeats().size() / 2, countNewSector);
        assertEquals(result.getSeats().size() / 2, countDefaultSector);
    }


    @Test
    @Transactional
    public void testGetRoomUsageForShow_standingSector_soldAndHeld() throws ValidationException {
        RoomDetailDto room = roomService.createRoom(createRoomDto);

        // get default sector
        SectorDto defaultSector = room.getSectors().stream()
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow();

        // replace default sector with standing sector
        StandingSectorDto standing = new StandingSectorDto();
        standing.setId(defaultSector.getId());
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

        // flush to ensure correct DB state before reading again
        roomRepository.flush();

        // Reload and get the correct sector type
        Sector reloadedSector = roomRepository.findById(room.getId()).orElseThrow()
            .getSectors().stream()
            .filter(s -> s instanceof StandingSector)
            .findFirst()
            .orElseThrow();

        assertInstanceOf(StandingSector.class, reloadedSector, "Sector must be StandingSector");
        StandingSector sectorEntity = (StandingSector) reloadedSector;

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

        StandingSectorUsageDto usageDto = usage.getSectors().stream()
            .filter(s -> s instanceof StandingSectorUsageDto)
            .map(s -> (StandingSectorUsageDto) s)
            .findFirst()
            .orElseThrow();

        assertEquals(5, usageDto.getAvailableCapacity(), "Should be 5 available from 10");
    }


    @Test
    public void testUpdateRoom_addNormalSector() throws ValidationException {
        RoomDetailDto original = roomService.createRoom(createRoomDto);

        SectorDto newSector = new SectorDto();
        newSector.setPrice(25);

        RoomDetailDto update = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(original.getId())
            .name(original.getName())
            .sectors(List.of(newSector))
            .seats(List.of())
            .eventLocationId(original.getEventLocationId())
            .build();

        RoomDetailDto updated = roomService.updateRoom(original.getId(), update);

        assertAll(
            () -> assertEquals(1, updated.getSectors().size()),
            () -> assertTrue(updated.getSectors().stream().anyMatch(s ->
                s instanceof SectorDto &&
                    !(s instanceof StandingSectorDto) &&
                    !(s instanceof StageSectorDto)
            ), "Expected a normal SectorDto to be present")
        );
    }

    @Test
    public void testUpdateRoom_addNormalSectorWithNonExistingId_throwsValidationException() {
        RoomDetailDto original = roomService.createRoom(createRoomDto);

        SectorDto invalidSector = new SectorDto();
        invalidSector.setId(9999L); // non-existent
        invalidSector.setPrice(30);

        RoomDetailDto update = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(original.getId())
            .name(original.getName())
            .sectors(List.of(invalidSector))
            .seats(original.getSeats())
            .eventLocationId(original.getEventLocationId())
            .build();

        assertThrows(ValidationException.class,
            () -> roomService.updateRoom(original.getId(), update));
    }

    @Test
    public void testUpdateRoom_addNormalSector_thenReassignSeats_thenChangeSectorTypeToStanding() throws ValidationException {
        RoomDetailDto original = roomService.createRoom(createRoomDto);

        SectorDto defaultSector = original.getSectors().get(0);

        SectorDto newNormalSector = new SectorDto();
        newNormalSector.setPrice(55);

        RoomDetailDto withNewSector = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(original.getId())
            .name(original.getName())
            .eventLocationId(original.getEventLocationId())
            .sectors(List.of(defaultSector, newNormalSector))
            .seats(original.getSeats())
            .build();

        RoomDetailDto updatedWithNormal = roomService.updateRoom(original.getId(), withNewSector);

        assertEquals(2, updatedWithNormal.getSectors().size());

        SectorDto createdNewNormal = updatedWithNormal.getSectors().stream()
            .filter(s -> !Objects.equals(s.getId(), defaultSector.getId()))
            .findFirst()
            .orElseThrow();

        List<SeatDto> reassignedSeats = updatedWithNormal.getSeats().stream()
            .map(seat -> {
                SeatDto updatedSeat = new SeatDto();
                updatedSeat.setId(seat.getId());
                updatedSeat.setRowNumber(seat.getRowNumber());
                updatedSeat.setColumnNumber(seat.getColumnNumber());
                updatedSeat.setDeleted(seat.isDeleted());
                updatedSeat.setSectorId(createdNewNormal.getId());
                return updatedSeat;
            })
            .toList();

        RoomDetailDto withSeatsReassigned = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(updatedWithNormal.getId())
            .name(updatedWithNormal.getName())
            .eventLocationId(updatedWithNormal.getEventLocationId())
            .sectors(List.of(defaultSector, createdNewNormal))
            .seats(reassignedSeats)
            .build();

        RoomDetailDto updatedWithSeats = roomService.updateRoom(updatedWithNormal.getId(), withSeatsReassigned);

        long countAssignedToNew = updatedWithSeats.getSeats().stream()
            .filter(s -> s.getSectorId().equals(createdNewNormal.getId()))
            .count();

        assertEquals(updatedWithSeats.getSeats().size(), countAssignedToNew);

        StandingSectorDto standing = new StandingSectorDto();
        standing.setId(createdNewNormal.getId());
        standing.setPrice(99);
        standing.setCapacity(50);

        RoomDetailDto withStanding = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(updatedWithSeats.getId())
            .name(updatedWithSeats.getName())
            .eventLocationId(updatedWithSeats.getEventLocationId())
            .sectors(List.of(defaultSector, standing))
            .seats(updatedWithSeats.getSeats())
            .build();

        RoomDetailDto finalResult = roomService.updateRoom(updatedWithSeats.getId(), withStanding);

        StandingSectorDto resultStanding = (StandingSectorDto) finalResult.getSectors().stream()
            .filter(s -> s instanceof StandingSectorDto)
            .findFirst()
            .orElseThrow();

        assertEquals(99, resultStanding.getPrice());
        assertEquals(50, resultStanding.getCapacity());
    }


    @Test
    public void testGetRoomUsageForShow_deletedSeatsAreNotAvailable() {
        RoomDetailDto room = roomService.createRoom(createRoomDto);

        Seat toDelete = seatRepository.findAll().get(0);
        toDelete.setDeleted(true);
        seatRepository.save(toDelete);

        Show show = Show.ShowBuilder.aShow()
            .withName("Deleted Test Show")
            .withDate(LocalDateTime.now().plusDays(1))
            .withDuration(100)
            .withEvent(event)
            .withRoom(roomRepository.findById(room.getId()).orElseThrow())
            .build();

        show = showRepository.save(show);

        RoomDetailDto usage = roomService.getRoomUsageForShow(show.getId());

        SeatUsageDto seatDto = usage.getSeats().stream()
            .map(s -> (SeatUsageDto) s)
            .filter(s -> s.getId().equals(toDelete.getId()))
            .findFirst()
            .orElseThrow();

        assertFalse(seatDto.isAvailable(), "Deleted seat should not be available");
    }

    @Test
    public void testGetRoomUsageForShow_reservedTicketBlocksSeat() {
        RoomDetailDto room = roomService.createRoom(createRoomDto);
        Seat reservedSeat = seatRepository.findAll().get(0);

        reservedSeat.setSector(sectorRepository.save(Sector.SectorBuilder.aSector()
            .withRoom(roomRepository.findById(room.getId()).orElseThrow())
            .withPrice(25)
            .build()));
        seatRepository.save(reservedSeat);

        Show show = Show.ShowBuilder.aShow()
            .withName("Reserved Test Show")
            .withDate(LocalDateTime.now().plusDays(1))
            .withDuration(120)
            .withEvent(event)
            .withRoom(roomRepository.findById(room.getId()).orElseThrow())
            .build();
        show = showRepository.save(show);

        Ticket reserved = new Ticket();
        reserved.setShow(show);
        reserved.setSeat(reservedSeat);
        reserved.setSector(reservedSeat.getSector());
        reserved.setStatus(TicketStatus.RESERVED);
        reserved.setCreatedAt(LocalDateTime.now());
        ticketRepository.save(reserved);

        RoomDetailDto usage = roomService.getRoomUsageForShow(show.getId());
        SeatUsageDto dto = usage.getSeats().stream()
            .map(s -> (SeatUsageDto) s)
            .filter(s -> s.getId().equals(reservedSeat.getId()))
            .findFirst()
            .orElseThrow();

        assertFalse(dto.isAvailable(), "Seat with RESERVED ticket should not be available");
    }

    @Test
    public void testGetRoomUsageForShow_expiredHold_doesNotBlockSeat() {
        RoomDetailDto room = roomService.createRoom(createRoomDto);
        Seat seatWithHold = seatRepository.findAll().get(0);

        seatWithHold.setSector(sectorRepository.save(Sector.SectorBuilder.aSector()
            .withRoom(roomRepository.findById(room.getId()).orElseThrow())
            .withPrice(20)
            .build()));
        seatRepository.save(seatWithHold);

        Show show = Show.ShowBuilder.aShow()
            .withName("Expired Hold Test Show")
            .withDate(LocalDateTime.now().plusDays(1))
            .withDuration(120)
            .withEvent(event)
            .withRoom(roomRepository.findById(room.getId()).orElseThrow())
            .build();
        show = showRepository.save(show);

        Hold hold = new Hold();
        hold.setSeatId(seatWithHold.getId());
        hold.setShowId(show.getId());
        hold.setSectorId(seatWithHold.getSector().getId());
        hold.setUserId(1L);
        hold.setValidUntil(LocalDateTime.now().minusMinutes(5)); // expired
        holdRepository.save(hold);

        RoomDetailDto usage = roomService.getRoomUsageForShow(show.getId());
        SeatUsageDto dto = usage.getSeats().stream()
            .map(s -> (SeatUsageDto) s)
            .filter(s -> s.getId().equals(seatWithHold.getId()))
            .findFirst()
            .orElseThrow();

        assertTrue(dto.isAvailable(), "Seat with expired hold should be available");
    }

    @Test
    @Transactional
    public void testGetRoomUsageForShow_standingSector_expiredHolds_doNotAffectAvailability() throws ValidationException {
        RoomDetailDto room = roomService.createRoom(createRoomDto);

        SectorDto defaultSector = room.getSectors().get(0);

        StandingSectorDto standing = new StandingSectorDto();
        standing.setPrice(20);
        standing.setCapacity(10);

        RoomDetailDto withStanding = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(room.getId())
            .name(room.getName())
            .eventLocationId(room.getEventLocationId())
            .sectors(List.of(defaultSector, standing))
            .seats(room.getSeats())
            .build();

        RoomDetailDto updatedRoom = roomService.updateRoom(room.getId(), withStanding);

        StandingSector standingSector = (StandingSector) roomRepository.findById(updatedRoom.getId()).get()
            .getSectors().stream()
            .filter(s -> s instanceof StandingSector)
            .findFirst()
            .orElseThrow();

        Show show = Show.ShowBuilder.aShow()
            .withName("Expired Hold Standing Test")
            .withDate(LocalDateTime.now().plusDays(1))
            .withDuration(90)
            .withEvent(event)
            .withRoom(roomRepository.findById(updatedRoom.getId()).orElseThrow())
            .build();
        show = showRepository.save(show);

        // 3 valid Tickets --> -3
        for (int i = 0; i < 3; i++) {
            Ticket ticket = new Ticket();
            ticket.setShow(show);
            ticket.setSector(standingSector);
            ticket.setStatus(TicketStatus.BOUGHT);
            ticketRepository.save(ticket);
        }

        // 2 expired Holds --> should be ignored
        for (int i = 0; i < 2; i++) {
            Hold expiredHold = new Hold();
            expiredHold.setSectorId(standingSector.getId());
            expiredHold.setShowId(show.getId());
            expiredHold.setUserId(1L);
            expiredHold.setValidUntil(LocalDateTime.now().minusMinutes(10));
            holdRepository.save(expiredHold);
        }

        RoomDetailDto usage = roomService.getRoomUsageForShow(show.getId());
        StandingSectorUsageDto standingUsage = (StandingSectorUsageDto) usage.getSectors().stream()
            .filter(s -> s instanceof StandingSectorUsageDto)
            .findFirst()
            .orElseThrow();

        assertEquals(7, standingUsage.getAvailableCapacity(), "Only tickets should reduce availability, expired holds ignored");
    }


    @Test
    public void testUpdateRoom_invalidDtoTypeCombination_throwsValidationException() {
        RoomDetailDto original = roomService.createRoom(createRoomDto);

        SectorDto invalid = new SectorDto(); // kein StageSectorDto
        invalid.setType(SectorType.STAGE);  // aber STAGE gesetzt!
        invalid.setPrice(50);

        RoomDetailDto update = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(original.getId())
            .name(original.getName())
            .sectors(List.of(invalid))
            .seats(original.getSeats())
            .eventLocationId(original.getEventLocationId())
            .build();

        assertThrows(ValidationException.class,
            () -> roomService.updateRoom(original.getId(), update));
    }

    @Test
    public void testUpdateRoom_addAdditionalStandingSector_resultsInTwoSectors() throws ValidationException {
        RoomDetailDto original = roomService.createRoom(createRoomDto);

        SectorDto defaultSector = original.getSectors().stream()
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow();

        StandingSectorDto additional = new StandingSectorDto();
        additional.setPrice(30);
        additional.setCapacity(50);

        RoomDetailDto updated = roomService.updateRoom(original.getId(), RoomDetailDto.RoomDetailDtoBuilder
            .aRoomDetailDto()
            .id(original.getId())
            .name(original.getName())
            .eventLocationId(original.getEventLocationId())
            .seats(original.getSeats())
            .sectors(List.of(defaultSector, additional))
            .build());

        assertEquals(2, updated.getSectors().size(), "There should be two sectors now (default + standing)");
        assertTrue(updated.getSectors().stream().anyMatch(s -> s instanceof StandingSectorDto),
            "One of the sectors should be a StandingSector");
    }

    @Test
    public void testGetRoomById_returnsCorrectRoom() {
        RoomDetailDto created = roomService.createRoom(createRoomDto);

        RoomDetailDto found = roomService.getRoomById(created.getId());

        assertAll(
            () -> assertEquals(created.getId(), found.getId()),
            () -> assertEquals(created.getName(), found.getName()),
            () -> assertEquals(created.getSectors().size(), found.getSectors().size()),
            () -> assertEquals(created.getSeats().size(), found.getSeats().size())
        );
    }

    @Test
    public void testReplaceSectorType_createsStageSector() throws ValidationException {
        RoomDetailDto created = roomService.createRoom(createRoomDto);

        SectorDto defaultSector = created.getSectors().get(0);

        StageSectorDto stage = new StageSectorDto();
        stage.setId(defaultSector.getId());

        RoomDetailDto updated = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(created.getId())
            .name(created.getName())
            .eventLocationId(created.getEventLocationId())
            .sectors(List.of(stage))
            .seats(created.getSeats())
            .build();

        RoomDetailDto result = roomService.updateRoom(created.getId(), updated);

        assertTrue(result.getSectors().stream().anyMatch(s -> s instanceof StageSectorDto),
            "Sector should be replaced by StageSector");
    }

    @Test
    public void testReplaceSectorType_createsNormalSector() throws ValidationException {
        RoomDetailDto created = roomService.createRoom(createRoomDto);

        SectorDto defaultSector = created.getSectors().get(0);

        SectorDto normal = new SectorDto();
        normal.setId(defaultSector.getId());
        normal.setPrice(50);

        RoomDetailDto updated = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(created.getId())
            .name(created.getName())
            .eventLocationId(created.getEventLocationId())
            .sectors(List.of(normal))
            .seats(created.getSeats())
            .build();

        RoomDetailDto result = roomService.updateRoom(created.getId(), updated);

        assertTrue(result.getSectors().stream().anyMatch(s -> s instanceof SectorDto &&
                !(s instanceof StandingSectorDto) && !(s instanceof StageSectorDto)),
            "Sector should be replaced by NormalSector");
    }

    @Test
    public void testSyncSectors_existingStageSector_syncsWithoutReplace() throws ValidationException {
        RoomDetailDto created = roomService.createRoom(createRoomDto);

        SectorDto defaultSector = created.getSectors().get(0);

        // Ersetze default mit StageSector
        StageSectorDto stage = new StageSectorDto();
        stage.setId(defaultSector.getId());

        RoomDetailDto withStage = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(created.getId())
            .name(created.getName())
            .eventLocationId(created.getEventLocationId())
            .sectors(List.of(stage))
            .seats(created.getSeats())
            .build();

        RoomDetailDto updated = roomService.updateRoom(created.getId(), withStage);

        // Jetzt nochmal mit selbem ID und StageSectorDto, um den Branch zu triggern:
        StageSectorDto sameStage = new StageSectorDto();
        sameStage.setId(updated.getSectors().get(0).getId());

        RoomDetailDto result = roomService.updateRoom(updated.getId(), RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(updated.getId())
            .name(updated.getName())
            .eventLocationId(updated.getEventLocationId())
            .sectors(List.of(sameStage))
            .seats(updated.getSeats())
            .build());

        assertTrue(result.getSectors().stream().anyMatch(s -> s instanceof StageSectorDto),
            "Existing StageSector should be synced without replacement");
    }

    @Test
    public void testSyncSectors_newStageSector_created() throws ValidationException {
        RoomDetailDto created = roomService.createRoom(createRoomDto);

        StageSectorDto stage = new StageSectorDto();

        List<SectorDto> existingSectors = created.getSectors();
        existingSectors.add(stage);

        RoomDetailDto withStage = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(created.getId())
            .name(created.getName())
            .eventLocationId(created.getEventLocationId())
            .sectors(existingSectors)
            .seats(created.getSeats())
            .build();

        RoomDetailDto result = roomService.updateRoom(created.getId(), withStage);

        assertTrue(result.getSectors().stream().anyMatch(s -> s instanceof StageSectorDto),
            "New StageSector should be created");
    }
}
