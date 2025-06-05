package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StageSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
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
            .rows(10)
            .columns(10)
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
    public void testUpdateRoom_removeSector_deletesIt() {
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
    public void testUpdateRoom_addStandingSector() {
        RoomDetailDto original = roomService.createRoom(createRoomDto);
        StandingSectorDto newSector = new StandingSectorDto();
        newSector.setPrice(55);
        newSector.setCapacity(45);

        RoomDetailDto update = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(original.getId())
            .name(original.getName())
            .sectors(new ArrayList<>(original.getSectors()) {{ add(newSector); }})
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
    public void testUpdateRoom_addStageSector() {
        RoomDetailDto original = roomService.createRoom(createRoomDto);
        StageSectorDto newSector = new StageSectorDto();
        newSector.setPrice(10);

        RoomDetailDto update = RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(original.getId())
            .name(original.getName())
            .sectors(new ArrayList<>(original.getSectors()) {{ add(newSector); }})
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

        assertThrows(EntityNotFoundException.class, () -> roomService.updateRoom(original.getId(), update));
    }

    @Test
    public void testGetAllRooms_returnsAllPersistedRooms() {
        roomService.createRoom(createRoomDto);
        roomService.createRoom(createRoomDto);

        List<RoomDetailDto> allRooms = roomService.getAllRooms();
        assertEquals(2, allRooms.size());
    }
}
