package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatUsageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatedSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorUsageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetSeatedDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetStandingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Hold;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.SeatedSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HoldRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SeatRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SectorRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final EventLocationRepository eventLocationRepository;
    private final RoomRepository roomRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomServiceImpl.class);
    private final SectorRepository sectorRepository;
    private final SeatRepository seatRepository;
    private final ShowService showService;
    private final TicketRepository ticketRepository;
    private final HoldRepository holdRepository;

    public RoomServiceImpl(EventLocationRepository eventLocationRepository,
                           RoomRepository roomRepository, SectorRepository sectorRepository, SeatRepository seatRepository, ShowService showService, TicketRepository ticketRepository, HoldRepository holdRepository) {
        this.eventLocationRepository = eventLocationRepository;
        this.roomRepository = roomRepository;
        this.sectorRepository = sectorRepository;
        this.seatRepository = seatRepository;
        this.showService = showService;
        this.ticketRepository = ticketRepository;
        this.holdRepository = holdRepository;
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

        syncSectors(room, dto.getSectors());

        roomRepository.saveAndFlush(room);
        return mapToDto(room);
    }

    @Override
    public Sector getSectorById(Long sectorId) {
        return sectorRepository.findById(sectorId)
            .orElseThrow(() -> new EntityNotFoundException("Sector not found with id " + sectorId));
    }

    @Override
    public Seat getSeatById(Long seatId) {
        return seatRepository.findById(seatId)
            .orElseThrow(() -> new EntityNotFoundException("Seat not found with id " + seatId));
    }

    @Override
    public RoomDetailDto getRoomById(Long id) {
        LOGGER.debug("Retrieving a room with details: {}", id);
        Room room = roomRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return mapToDto(room);
    }

    @Override
    public RoomDetailDto getRoomUsageForShow(Long showId) {
        LOGGER.debug("Retrieving room usage for show with id: {}", showId);

        Show show = showService.getShowWithRoomAndSectors(showId);
        Room room = show.getRoom();

        // tickets sold
        List<Ticket> tickets = ticketRepository.findByShowId(showId);

        tickets = tickets.stream()
                .filter(t -> (t.getStatus() == TicketStatus.BOUGHT) || (t.getStatus() == TicketStatus.RESERVED))
                .toList();

        Set<Long> occupiedSeatIds = tickets.stream()
                .map(Ticket::getSeat)
                .filter(Objects::nonNull)

                .map(Seat::getId)
                .collect(Collectors.toSet());
        Map<Long, Long> soldStandingCounts = tickets.stream()
                .filter(t -> t.getSeat() == null)
                .collect(Collectors.groupingBy(
                        t -> t.getSector().getId(),
                        Collectors.counting()
                ));

        // holds that are still valid
        List<Hold> validHolds = holdRepository.findByShowId(showId).stream()
                .filter(h -> h.getValidUntil().isAfter(LocalDateTime.now()))
                .toList();

        // separate out held seats vs. held standing spots
        Set<Long> heldSeatIds = validHolds.stream()
                .map(Hold::getSeatId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Long> standingHoldCounts = validHolds.stream()
                .filter(h -> h.getSeatId() == null)
                .collect(Collectors.groupingBy(
                        Hold::getSectorId,
                        Collectors.counting()
                ));

        List<SectorDto> usageSectors = new ArrayList<>();

        for (Sector sec : room.getSectors()) {
            if (sec instanceof SeatedSector seated) {
                List<SeatUsageDto> seatDtos = seated.getSeats().stream()
                        .map(seat -> {
                            SeatUsageDto dto = new SeatUsageDto();
                            dto.setId(seat.getId());
                            dto.setRowNumber(seat.getRowNumber());
                            dto.setColumnNumber(seat.getColumnNumber());
                            dto.setDeleted(seat.isDeleted());
                            // unavailable if sold OR held

                            boolean isNotOccupiedByTicket = !occupiedSeatIds.contains(seat.getId());
                            boolean isNotOccupiedByHold = !heldSeatIds.contains(seat.getId());

                            boolean isAvailable = isNotOccupiedByTicket && isNotOccupiedByHold;

                            dto.setAvailable(isAvailable);
                            return dto;
                        })
                        .toList();

                SeatedSectorDto sdto = SeatedSectorDto.SeatedSectorDtoBuilder
                        .aSeatedSectorDto()
                        .id(seated.getId())
                        .price(seated.getPrice())
                        .rows(seatDtos)
                        .build();

                usageSectors.add(sdto);

            } else if (sec instanceof StandingSector standing) {
                long sold    = soldStandingCounts.getOrDefault(standing.getId(), 0L);
                long held    = standingHoldCounts.getOrDefault(standing.getId(), 0L);
                int capacity = standing.getCapacity();

                int availableCapacity = capacity
                        - Math.toIntExact(sold)
                        - Math.toIntExact(held);

                StandingSectorUsageDto udto = new StandingSectorUsageDto();
                udto.setId(standing.getId());
                udto.setPrice(standing.getPrice());
                udto.setCapacity(capacity);
                udto.setAvailableCapacity(availableCapacity);

                usageSectors.add(udto);
            }
        }

        return RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
                .id(room.getId())
                .name(room.getName())
                .sectors(usageSectors)
                .build();
    }

    @Override
    public List<RoomDetailDto> getAllRooms() {
        LOGGER.info("Fetching all rooms");
        return roomRepository.findAll().stream()
            .map(this::mapToDto)
            .toList();
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
        room.setEventLocation(location);

        for (int i = 0; i < dto.getNumberOfSectors(); i++) {
            SeatedSector sector = new SeatedSector();
            sector.setPrice(10);
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
        if (dto.getId() != null && !existing.containsKey(dto.getId())) {
            throw new EntityNotFoundException("SeatedSector not found with id " + dto.getId());
        }
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
        if (dto.getId() != null && !existing.containsKey(dto.getId())) {
            throw new EntityNotFoundException("SeatedSector not found with id " + dto.getId());
        }
        SeatedSector sec = (SeatedSector) existing.getOrDefault(dto.getId(), new SeatedSector());
        sec.setPrice(dto.getPrice());
        sec.setRoom(room);

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
