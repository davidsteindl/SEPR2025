package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatUsageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StageSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorUsageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.RoomMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.SeatMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.SectorMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Hold;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.StageSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HoldRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SeatRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SectorRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.SectorValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final RoomMapper roomMapper;
    private final SectorMapper sectorMapper;
    private final SeatMapper seatMapper;
    private final SectorValidator sectorValidator;

    @Autowired
    public RoomServiceImpl(EventLocationRepository eventLocationRepository,
                           RoomRepository roomRepository, SectorRepository sectorRepository, SeatRepository seatRepository, ShowService showService,
                           TicketRepository ticketRepository, HoldRepository holdRepository, RoomMapper roomMapper, SectorMapper sectorMapper,
                           SeatMapper seatMapper, SectorValidator sectorValidator) {
        this.eventLocationRepository = eventLocationRepository;
        this.roomRepository = roomRepository;
        this.sectorRepository = sectorRepository;
        this.seatRepository = seatRepository;
        this.showService = showService;
        this.ticketRepository = ticketRepository;
        this.holdRepository = holdRepository;
        this.roomMapper = roomMapper;
        this.sectorMapper = sectorMapper;
        this.seatMapper = seatMapper;
        this.sectorValidator = sectorValidator;
    }

    @Override
    @Transactional
    public RoomDetailDto createRoom(CreateRoomDto dto) {
        LOGGER.info("Creating a new room with details: {}", dto);
        EventLocation location = eventLocationRepository.findById(dto.getEventLocationId())
            .orElseThrow(() -> new EntityNotFoundException(
                "EventLocation not found with id " + dto.getEventLocationId()));

        Room room = Room.RoomBuilder.aRoom()
            .withName(dto.getName())
            .withEventLocation(location)
            .build();
        Room savedRoom = roomRepository.save(room);

        //Adds default sector for seats
        Sector defaultSector = new Sector();
        defaultSector.setPrice(1);
        defaultSector.setRoom(savedRoom);
        savedRoom.addSector(defaultSector);

        for (int i = 0; i < dto.getRows(); i++) {
            for (int j = 0; j < dto.getColumns(); j++) {
                Seat seat = Seat.SeatBuilder.aSeat()
                    .withRowNumber(i + 1)
                    .withColumnNumber(j + 1)
                    .withDeleted(false)
                    .withSector(defaultSector)
                    .withRoom(savedRoom)
                    .build();
                savedRoom.addSeat(seat);
            }
        }
        return roomMapper.roomToRoomDetailDto(savedRoom);
    }

    @Override
    @Transactional
    public RoomDetailDto updateRoom(Long id, RoomDetailDto dto) throws ValidationException {
        LOGGER.info("Updating a room with details: {}", dto);
        if (!Objects.equals(id, dto.getId())) {
            throw new IllegalArgumentException("ID in path and payload must match");
        }

        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Room not found: " + id));

        List<SectorDto> dtoSectors = dto.getSectors();
        if (!dtoSectors.isEmpty()) {
            for (SectorDto sectorDto : dtoSectors) {
                sectorValidator.validateSector(sectorDto);
            }
        }

        room.setName(dto.getName());

        room.setEventLocation(
            eventLocationRepository.findById(dto.getEventLocationId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "EventLocation not found with id " + dto.getEventLocationId())));

        syncSeats(room, dto.getSeats());

        syncSectors(room, dtoSectors);

        roomRepository.saveAndFlush(room);
        return roomMapper.roomToRoomDetailDto(room);
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
    @Transactional
    public RoomDetailDto getRoomById(Long id) {
        LOGGER.debug("Retrieving a room with details: {}", id);
        Room room = roomRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return roomMapper.roomToRoomDetailDto(room);
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

        // prepare full seat list with availability info
        List<SeatUsageDto> seatDtos = room.getSeats().stream()
            .map(seat -> {
                SeatUsageDto dto = new SeatUsageDto();
                dto.setId(seat.getId());
                dto.setRowNumber(seat.getRowNumber());
                dto.setColumnNumber(seat.getColumnNumber());
                dto.setDeleted(seat.isDeleted());
                dto.setRoomId(room.getId());
                dto.setSectorId(seat.getSector() != null ? seat.getSector().getId() : null);

                // only seats that belong to a normal sector are bookable
                boolean isBookable = seat.getSector() != null && seat.getSector().isBookable();
                boolean isAvailable = isBookable
                    && !seat.isDeleted()
                    && !occupiedSeatIds.contains(seat.getId())
                    && !heldSeatIds.contains(seat.getId());

                dto.setAvailable(isAvailable);
                return dto;
            })
            .toList();

        List<SectorDto> usageSectors = new ArrayList<>();

        for (Sector sec : room.getSectors()) {
            if (sec instanceof StandingSector standing) {
                long sold = soldStandingCounts.getOrDefault(standing.getId(), 0L);
                long held = standingHoldCounts.getOrDefault(standing.getId(), 0L);
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

            } else if (sec instanceof StageSector stage) {
                StageSectorDto dto = new StageSectorDto();
                dto.setId(stage.getId());
                if (stage.getPrice() != null) {
                    dto.setPrice(stage.getPrice());
                } else {
                    dto.setPrice(0);
                }
                usageSectors.add(dto);

            } else {
                SectorDto dto = new SectorDto();
                dto.setId(sec.getId());
                if (sec.getPrice() != null) {
                    dto.setPrice(sec.getPrice());
                } else {
                    dto.setPrice(0);
                }
                usageSectors.add(dto);
            }
        }

        return RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(room.getId())
            .name(room.getName())
            .sectors(usageSectors)
            .seats(seatDtos.stream().map(seat -> (SeatDto) seat).toList())
            .eventLocationId(room.getEventLocation().getId())
            .build();
    }


    public List<RoomDetailDto> getAllRooms() {
        LOGGER.info("Fetching all rooms");
        return roomRepository.findAllWithSectorsAndSeats().stream()
            .map(roomMapper::roomToRoomDetailDto)
            .toList();
    }

    /**
     * Synchronizes the Room's seats with the given list of SeatDto: updates existing,
     * adds new ones, and removes those not present in the DTO.
     *
     * @param room     the managed Room entity
     * @param seatDtos list of SeatDto representing desired final state
     */
    private void syncSeats(Room room, List<SeatDto> seatDtos) {
        LOGGER.debug("Syncing seats for room ID {} with {} seat DTOs", room.getId(), seatDtos.size());

        Map<Long, Seat> existingSeatsById = room.getSeats().stream()
            .filter(seat -> seat.getId() != null)
            .collect(Collectors.toMap(Seat::getId, Function.identity()));

        List<Seat> updatedSeatList = new ArrayList<>();

        for (SeatDto dto : seatDtos) {
            Seat seat;

            if (dto.getId() != null) {
                seat = existingSeatsById.get(dto.getId());
                if (seat == null) {
                    throw new EntityNotFoundException("Seat not found with id " + dto.getId());
                }
            } else {
                seat = new Seat();
                room.addSeat(seat);
            }

            seat.setRowNumber(dto.getRowNumber());
            seat.setColumnNumber(dto.getColumnNumber());
            seat.setDeleted(dto.isDeleted());

            updatedSeatList.add(seat);
        }

        room.getSeats().removeIf(seat -> seat.getId() != null && updatedSeatList.stream().noneMatch(s -> Objects.equals(s.getId(), seat.getId())));
    }

    /**
     * Synchronizes the Room's sectors with the given list of SectorDto: updates existing,
     * adds new ones, and removes those not present in the DTO.
     *
     * @param room       the managed Room entity
     * @param sectorDtos list of SectorDto representing desired final state
     */
    private void syncSectors(Room room, List<SectorDto> sectorDtos) {
        LOGGER.debug("Syncing the sectors with new data: {}", sectorDtos);
        Map<Long, Sector> existing = room.getSectors().stream()
            .collect(Collectors.toMap(Sector::getId, Function.identity()));
        List<Sector> toKeep = new ArrayList<>();

        for (SectorDto sd : sectorDtos) {
            Sector sector;
            if (sd instanceof StandingSectorDto ssd) {
                sector = syncStanding(existing, room, ssd);
            } else if (sd instanceof StageSectorDto stgd) {
                sector = syncStage(existing, room, stgd);
            } else if (sd instanceof SectorDto normal) {
                sector = syncNormalSector(existing, room, normal);
            } else {
                throw new IllegalArgumentException("Unknown sector DTO type: " + sd.getClass());
            }
            toKeep.add(sector);
        }

        room.getSectors().removeIf(exists ->
            toKeep.stream().noneMatch(s -> Objects.equals(s.getId(), exists.getId())));
    }

    /**
     * Synchronizes or creates a normal Sector based on the DTO.
     *
     * @param existing map of existing sectors by ID
     * @param room     the Room to attach new sectors to
     * @param dto      the SectorDto containing updated data
     * @return the managed Sector instance
     */
    private Sector syncNormalSector(Map<Long, Sector> existing, Room room, SectorDto dto) {
        LOGGER.debug("Syncing normal sector with details: {}", dto);

        Sector sec;
        if (dto.getId() != null) {
            if (!existing.containsKey(dto.getId())) {
                throw new EntityNotFoundException("Sector not found with id " + dto.getId());
            }
            sec = existing.get(dto.getId());
        } else {
            sec = new Sector();
            room.addSector(sec);
        }

        sec.setPrice(dto.getPrice());
        sec.setRoom(room);
        return sec;
    }


    /**
     * Synchronizes or creates a StandingSector based on the DTO.
     *
     * @param existing map of existing sectors by ID
     * @param room     the Room to attach new sectors to
     * @param dto      the StandingSectorDto containing updated data
     * @return the managed StandingSector instance
     */
    private StandingSector syncStanding(Map<Long, Sector> existing, Room room, StandingSectorDto dto) {
        LOGGER.debug("Syncing the standing sector with details: {}", dto);
        if (dto.getId() != null && !existing.containsKey(dto.getId())) {
            throw new EntityNotFoundException("SeatedSector not found with id " + dto.getId());
        }
        Sector raw = dto.getId() != null ? existing.get(dto.getId()) : null;

        StandingSector sec;

        // Ensure the sector is of type StandingSector:
        // If an existing sector has a different type, delete and replace it
        // If no sector exists, create a new one
        // Otherwise, reuse the existing StandingSector
        if (raw != null && !(raw instanceof StandingSector)) {
            sectorRepository.delete(raw);
            room.getSectors().remove(raw);
            sec = new StandingSector();
            room.addSector(sec);
        } else if (raw == null) {
            sec = new StandingSector();
            room.addSector(sec);
        } else {
            sec = (StandingSector) raw;
        }

        sec.setPrice(dto.getPrice());
        sec.setCapacity(dto.getCapacity());
        sec.setRoom(room);
        if (dto.getId() == null) {
            room.addSector(sec);
        }
        return sec;
    }

    /**
     * Synchronizes or creates a StageSector based on the DTO.
     *
     * @param existing map of existing sectors by ID
     * @param room     the Room to attach new sectors to
     * @param dto      the StageSectorDto containing updated data
     * @return the managed StageSector instance
     */
    private StageSector syncStage(Map<Long, Sector> existing, Room room, StageSectorDto dto) {
        LOGGER.debug("Syncing the seated sector with details: {}", dto);
        if (dto.getId() != null && !existing.containsKey(dto.getId())) {
            throw new EntityNotFoundException("SeatedSector not found with id " + dto.getId());
        }
        StageSector sec = (StageSector) existing.getOrDefault(dto.getId(), new StageSector());
        sec.setRoom(room);
        if (dto.getId() == null) {
            room.addSector(sec);
        }
        return sec;
    }
}
