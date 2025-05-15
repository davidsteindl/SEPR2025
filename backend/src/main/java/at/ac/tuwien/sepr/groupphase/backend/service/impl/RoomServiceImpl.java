package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatedSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.SeatedSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final EventLocationRepository eventLocationRepository;
    private final RoomRepository roomRepository;
    private final static Logger LOGGER = LoggerFactory.getLogger(RoomServiceImpl.class);

    public RoomServiceImpl(EventLocationRepository eventLocationRepository,
                           RoomRepository roomRepository) {
        this.eventLocationRepository = eventLocationRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public RoomDetailDto createRoom(CreateRoomDto dto) {
        LOGGER.info("Creating a new room with details: {}", dto);
        EventLocation location = eventLocationRepository.findById(dto.getEventLocationId())
            .orElseThrow(() -> new EntityNotFoundException(
                "EventLocation not found with id " + dto.getEventLocationId()));

        Room room = buildNewRoom(dto, location);
        Room saved = roomRepository.save(room);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public RoomDetailDto updateRoom(Long id, RoomDetailDto dto) {
        LOGGER.info("Updating a room with details: {}", dto);
        if (!Objects.equals(id, dto.getId())) {
            throw new IllegalArgumentException("ID in path and payload must match");
        }

        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + id));

        room.setName(dto.getName());
        room.setHorizontal(dto.isHorizontal());

        syncSectors(room, dto.getSectors());

        roomRepository.saveAndFlush(room);
        return mapToDto(room);
    }


    /**
     * Constructs a new Room entity from the provided DTO and EventLocation.
     *
     * @param dto the CreateRoomDto containing room setup parameters
     * @param location the EventLocation to associate with the new Room
     * @return a fully initialized Room entity (not yet persisted)
     */
    private Room buildNewRoom(CreateRoomDto dto, EventLocation location) {
        LOGGER.debug("Building a new room with details: {}", dto);
        Room room = new Room();
        room.setName(dto.getName());
        room.setHorizontal(dto.isHorizontal());
        room.setEventLocation(location);

        for (int i = 0; i < dto.getNumberOfSectors(); i++) {
            SeatedSector sector = new SeatedSector();
            sector.setPrice(0);
            buildSeats(dto, sector);
            room.addSector(sector);
        }
        return room;
    }

    /**
     * Populates the given SeatedSector with Seat entities based on DTO dimensions.
     *
     * @param dto the CreateRoomDto containing rowsPerSector and seatsPerRow
     * @param sector the SeatedSector to populate
     */
    private void buildSeats(CreateRoomDto dto, SeatedSector sector) {
        LOGGER.debug("Populating the seated sector with details: {}", dto);
        for (int r = 1; r <= dto.getRowsPerSector(); r++) {
            for (int c = 1; c <= dto.getSeatsPerRow(); c++) {
                Seat seat = new Seat();
                seat.setRowNumber(r);
                seat.setColumnNumber(c);
                seat.setDeleted(false);
                sector.addSeat(seat);
            }
        }
    }

    /**
     * Synchronizes the Room's sectors with the given list of SectorDto: updates existing,
     * adds new ones, and removes those not present in the DTO.
     *
     * @param room the managed Room entity
     * @param sectorDtos list of SectorDto representing desired final state
     */
    private void syncSectors(Room room, List<SectorDto> sectorDtos) {
        LOGGER.debug("Syncing the sectors with new data: {}", sectorDtos);
        Map<Long, Sector> existing = room.getSectors().stream()
            .collect(Collectors.toMap(Sector::getId, Function.identity()));
        List<Sector> toKeep = new ArrayList<>();

        for (SectorDto sd : sectorDtos) {
            Sector sector = switch (sd) {
                case StandingSectorDto ssd -> syncStanding(existing, room, ssd);
                case SeatedSectorDto sed -> syncSeated(existing, room, sed);
                default -> throw new IllegalArgumentException("Unknown sector DTO type: " + sd.getClass());
            };
            toKeep.add(sector);
        }

        room.getSectors().retainAll(toKeep);
    }

    /**
     * Synchronizes or creates a StandingSector based on the DTO.
     *
     * @param existing map of existing sectors by ID
     * @param room the Room to attach new sectors to
     * @param dto the StandingSectorDto containing updated data
     * @return the managed StandingSector instance
     */
    private StandingSector syncStanding(Map<Long, Sector> existing, Room room, StandingSectorDto dto) {
        LOGGER.debug("Syncing the standing sector with details: {}", dto);
        StandingSector sec = (StandingSector) existing.getOrDefault(dto.getId(), new StandingSector());
        sec.setPrice(dto.getPrice());
        sec.setCapacity(dto.getCapacity());
        sec.setRoom(room);
        if (dto.getId() == null) {
            room.addSector(sec);
        }
        return sec;
    }

    /**
     * Synchronizes or creates a SeatedSector and its Seat children based on the DTO.
     *
     * @param existing map of existing sectors by ID
     * @param room the Room to attach new sectors to
     * @param dto the SeatedSectorDto containing updated data
     * @return the managed SeatedSector instance
     */
    private SeatedSector syncSeated(Map<Long, Sector> existing, Room room, SeatedSectorDto dto) {
        LOGGER.debug("Syncing the seated sector with details: {}", dto);
        SeatedSector sec = (SeatedSector) existing.getOrDefault(dto.getId(), new SeatedSector());
        sec.setPrice(dto.getPrice());
        sec.setRoom(room);

        // sync seats
        Map<Long, Seat> old = sec.getSeats().stream()
            .collect(Collectors.toMap(Seat::getId, Function.identity()));
        List<Seat> updated = new ArrayList<>();
        for (SeatDto sd : dto.getRows()) {
            Seat seat = old.getOrDefault(sd.getId(), new Seat());
            seat.setRowNumber(sd.getRowNumber());
            seat.setColumnNumber(sd.getColumnNumber());
            seat.setDeleted(sd.isDeleted());
            seat.setSector(sec);
            updated.add(seat);
        }
        sec.getSeats().clear();
        sec.getSeats().addAll(updated);

        if (dto.getId() == null) {
            room.addSector(sec);
        }
        return sec;
    }

    /**
     * Maps a Room entity (and its sectors) to a RoomDetailDto.
     *
     * @param room the Room to map
     * @return the RoomDetailDto representation
     */
    private RoomDetailDto mapToDto(Room room) {
        LOGGER.debug("Mapping a room details: {}", room);
        List<SectorDto> sectors = room.getSectors().stream()
            .map(this::mapSector)
            .collect(Collectors.toList());

        return RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(room.getId())
            .name(room.getName())
            .sectors(sectors)
            .isHorizontal(room.isHorizontal())
            .build();
    }

    /**
     * Converts a Sector entity to its appropriate DTO subclass.
     *
     * @param sector the Sector to convert
     * @return a SeatedSectorDto or StandingSectorDto
     */
    private SectorDto mapSector(Sector sector) {
        LOGGER.debug("Mapping a sector details: {}", sector);
        if (sector instanceof SeatedSector seated) {
            List<SeatDto> seats = seated.getSeats().stream()
                .map(this::mapSeat)
                .collect(Collectors.toList());
            return SeatedSectorDto.SeatedSectorDtoBuilder.aSeatedSectorDto()
                .id(seated.getId())
                .price(seated.getPrice())
                .rows(seats)
                .build();

        } else if (sector instanceof StandingSector standing) {
            return StandingSectorDto.StandingSectorDtoBuilder.aStandingSectorDto()
                .id(standing.getId())
                .price(standing.getPrice())
                .capacity(standing.getCapacity())
                .build();
        } else {
            throw new IllegalArgumentException("Unknown Sector type: " + sector.getClass());
        }
    }

    /**
     * Maps a Seat entity to a SeatDto.
     *
     * @param seat the Seat to map
     * @return the SeatDto representation
     */
    private SeatDto mapSeat(Seat seat) {
        LOGGER.debug("Mapping a seat details: {}", seat);
        return SeatDto.SeatDtoBuilder.aSeatDto()
            .id(seat.getId())
            .rowNumber(seat.getRowNumber())
            .columnNumber(seat.getColumnNumber())
            .deleted(seat.isDeleted())
            .build();
    }
}
