package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SeatRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SectorRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private SectorRepository sectorRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowService showService;

    @Autowired
    private TicketRepository ticketRepository;

    // <-- let Spring inject the @Service (and its @Transactional proxy)
    @Autowired
    private RoomService roomService;

    private EventLocation testLocation;
    private CreateRoomDto createRoomDto;

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

        createRoomDto = CreateRoomDto.CreateRoomDtoBuilder
            .aCreateRoomDtoBuilder()
            .eventLocationId(testLocation.getId())
            .name("Room A")
            .numberOfSectors(2)
            .rowsPerSector(3)
            .seatsPerRow(4)
            .build();
    }

    @AfterEach
    public void tearDown() {
        roomRepository.deleteAll();
        eventLocationRepository.deleteAll();
    }

    @Test
    public void testCreateRoom_validInput_createsRoom() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);
        assertAll(
            () -> assertNotNull(result.getId(), "Room ID should be generated"),
            () -> assertEquals("Room A", result.getName(), "Room name should match input"),
            () -> assertEquals(2, result.getSectors().size(), "Should create two sectors")
        );
    }

    @Test
    public void testCreateRoom_validInput_createsRoom_validId() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);
        assertNotNull(result.getId(), "Generated RoomDetailDto ID should not be null");
    }

    @Test
    public void testCreateRoom_validInput_createsRoom_hasTwoSectors() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);
        assertEquals(2, result.getSectors().size(), "There should be exactly two sectors");
    }

    @Test
    public void testCreateRoom_validInput_createsRoom_allSectorsAreSeatedSectors() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);
        for (SectorDto sector : result.getSectors()) {
            assertInstanceOf(SeatedSectorDto.class, sector,
                "All sectors should be SeatedSectorDto instances");
        }
    }

    @Test
    public void testCreateRoom_validInput_createsRoom_allSectorsHaveCorrectSeatCount() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);
        int expectedSeats = createRoomDto.getRowsPerSector() * createRoomDto.getSeatsPerRow();
        for (SectorDto sector : result.getSectors()) {
            SeatedSectorDto seated = (SeatedSectorDto) sector;
            List<SeatDto> seats = seated.getRows();
            assertEquals(expectedSeats, seats.size(),
                "Each sector should contain rowsPerSector*seatsPerRow seats");
        }
    }

    @Test
    public void testCreateRoom_validInput_seatsHaveCorrectRowAndColumnNumbers() {
        RoomDetailDto result = roomService.createRoom(createRoomDto);
        int rows = createRoomDto.getRowsPerSector();
        int cols = createRoomDto.getSeatsPerRow();
        for (SectorDto sector : result.getSectors()) {
            SeatedSectorDto seated = (SeatedSectorDto) sector;
            for (SeatDto seat : seated.getRows()) {
                assertTrue(seat.getRowNumber() >= 1 && seat.getRowNumber() <= rows,
                    "Row number should be between 1 and rowsPerSector");
                assertTrue(seat.getColumnNumber() >= 1 && seat.getColumnNumber() <= cols,
                    "Column number should be between 1 and seatsPerRow");
            }
        }
    }

    @Test
    public void testCreateRoom_eventLocationNotFound_throws() {
        CreateRoomDto dto = CreateRoomDto.CreateRoomDtoBuilder
            .aCreateRoomDtoBuilder()
            .eventLocationId(999L)
            .name("Room X")
            .numberOfSectors(1)
            .rowsPerSector(1)
            .seatsPerRow(1)
            .build();

        assertThrows(EntityNotFoundException.class,
            () -> roomService.createRoom(dto),
            "Should throw if EventLocation not found");
        assertEquals(0, roomRepository.findAll().size(),
            "No rooms should be persisted on error");
    }

    @Test
    public void testUpdateRoom_addStandingSector() {
        RoomDetailDto original = roomService.createRoom(createRoomDto);
        Long roomId = original.getId();

        List<SectorDto> updatedSectors = original.getSectors().stream()
            .map(s -> {
                if (s instanceof SeatedSectorDto sed) {
                    return SeatedSectorDto.SeatedSectorDtoBuilder.aSeatedSectorDto()
                        .id(s.getId())
                        .price(s.getPrice())
                        .rows(sed.getRows())
                        .build();
                } else {
                    throw new IllegalStateException("unexpected sector type");
                }
            })
            .collect(Collectors.toList());

        StandingSectorDto newStanding = StandingSectorDto.StandingSectorDtoBuilder.aStandingSectorDto()
            .id(null)
            .price(10)
            .capacity(50)
            .build();
        updatedSectors.add(newStanding);

        RoomDetailDto toUpdate = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(roomId)
            .name(original.getName())
            .sectors(updatedSectors)
            .build();

        RoomDetailDto result = roomService.updateRoom(roomId, toUpdate);
        long countStanding = result.getSectors().stream()
            .filter(s -> s instanceof StandingSectorDto)
            .count();
        assertAll(
            () -> assertEquals(3, result.getSectors().size(), "Should now have three sectors"),
            () -> assertEquals(1, countStanding, "Exactly one standing sector should have been added")
        );
    }

    @Test
    public void testUpdateRoom_addSeatedSector() {

        RoomDetailDto original = roomService.createRoom(createRoomDto);
        Long roomId = original.getId();

        List<SectorDto> updatedSectors = new ArrayList<>();
        for (SectorDto s : original.getSectors()) {
            SeatedSectorDto sed = (SeatedSectorDto) s;
            updatedSectors.add(
                SeatedSectorDto.SeatedSectorDtoBuilder.aSeatedSectorDto()
                    .id(sed.getId())
                    .price(sed.getPrice())
                    .rows(sed.getRows())
                    .build()
            );
        }

        // new seated sector with 2×2 seat layout
        List<SeatDto> newSeats = List.of(
            SeatDto.SeatDtoBuilder.aSeatDto().id(null).rowNumber(1).columnNumber(1).deleted(false).build(),
            SeatDto.SeatDtoBuilder.aSeatDto().id(null).rowNumber(1).columnNumber(2).deleted(false).build(),
            SeatDto.SeatDtoBuilder.aSeatDto().id(null).rowNumber(2).columnNumber(1).deleted(false).build(),
            SeatDto.SeatDtoBuilder.aSeatDto().id(null).rowNumber(2).columnNumber(2).deleted(false).build()
        );
        SeatedSectorDto newSeated = SeatedSectorDto.SeatedSectorDtoBuilder.aSeatedSectorDto()
            .id(null)
            .price(20)
            .rows(newSeats)
            .build();
        updatedSectors.add(newSeated);

        RoomDetailDto toUpdate = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(roomId)
            .name(original.getName())
            .sectors(updatedSectors)
            .build();

        RoomDetailDto result = roomService.updateRoom(roomId, toUpdate);

        assertEquals(3, result.getSectors().size(), "Should now have three sectors total");
        long countSeated = result.getSectors().stream()
            .filter(s -> s instanceof SeatedSectorDto)
            .count();
        assertEquals(3, countSeated, "All sectors should be SeatedSectorDto, including the new one");
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
    public void testUpdateRoom_removeSector_deletesIt() {
        RoomDetailDto original = roomService.createRoom(createRoomDto);
        Long roomId = original.getId();

        Long removedSectorId = original.getSectors().get(0).getId();

        SectorDto keep = original.getSectors().get(1);
        RoomDetailDto toUpdate = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(roomId)
            .name(original.getName())
            .sectors(List.of(keep))
            .build();

        RoomDetailDto result = roomService.updateRoom(roomId, toUpdate);

        assertEquals(1, result.getSectors().size(), "Should have only one sector after removal");
        assertFalse(
            result.getSectors().stream()
                .map(SectorDto::getId)
                .anyMatch(id -> id.equals(removedSectorId)),
            "Removed sector ID should no longer be present"
        );

        Room persisted = roomRepository.findById(roomId).orElseThrow();
        assertEquals(1, persisted.getSectors().size(), "JPA should now have exactly one sector");
    }

    @Test
    public void testUpdateRoom_addSectorWithNonExistingId_throwsEntityNotFoundException() {

        RoomDetailDto original = roomService.createRoom(createRoomDto);
        Long roomId = original.getId();

        List<SectorDto> updatedSectors = new ArrayList<>(original.getSectors());
        StandingSectorDto bogus = StandingSectorDto.StandingSectorDtoBuilder.aStandingSectorDto()
            .id(999L)
            .price(15)
            .capacity(30)
            .build();
        updatedSectors.add(bogus);

        RoomDetailDto toUpdate = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(roomId)
            .name( original.getName() )
            .sectors( updatedSectors )
            .build();

        assertThrows(EntityNotFoundException.class,
            () -> roomService.updateRoom(roomId, toUpdate));
    }

    @Test
    public void testGetAllRooms_returnsAllPersistedRooms() {
        RoomDetailDto room1 = roomService.createRoom(createRoomDto);

        CreateRoomDto secondRoomDto = CreateRoomDto.CreateRoomDtoBuilder
            .aCreateRoomDtoBuilder()
            .eventLocationId(testLocation.getId())
            .name("Room B")
            .numberOfSectors(1)
            .rowsPerSector(2)
            .seatsPerRow(3)
            .build();
        RoomDetailDto room2 = roomService.createRoom(secondRoomDto);

        List<RoomDetailDto> rooms = roomService.getAllRooms();

        assertNotNull(rooms, "Returned room list should not be null");
        assertEquals(2, rooms.size(), "Should return exactly 2 rooms");
        List<String> roomNames = rooms.stream().map(RoomDetailDto::getName).toList();
        assertTrue(roomNames.contains("Room A"));
        assertTrue(roomNames.contains("Room B"));
    }


}
