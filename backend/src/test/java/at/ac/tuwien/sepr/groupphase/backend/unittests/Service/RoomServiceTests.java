package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatedSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.RoomServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class RoomServiceTests {

    @Autowired
    private EventLocationRepository eventLocationRepository;

    @Autowired
    private RoomRepository roomRepository;

    private RoomServiceImpl roomService;
    private EventLocation testLocation;
    private CreateRoomDto createRoomDto;

    @BeforeEach
    public void setUp() {
        roomService = new RoomServiceImpl(eventLocationRepository, roomRepository);
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
            .isHorizontal(true)
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
            () -> assertTrue(result.isHorizontal(), "Horizontal flag should be true"),
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
            .isHorizontal(false)
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
}
