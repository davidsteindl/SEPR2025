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
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Hold;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.StageSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
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
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final SectorValidator sectorValidator;

    @Autowired
    public RoomServiceImpl(EventLocationRepository eventLocationRepository,
                           RoomRepository roomRepository, SectorRepository sectorRepository, SeatRepository seatRepository, ShowService showService,
                           TicketRepository ticketRepository, HoldRepository holdRepository, RoomMapper roomMapper, SectorValidator sectorValidator) {
        this.eventLocationRepository = eventLocationRepository;
        this.roomRepository = roomRepository;
        this.sectorRepository = sectorRepository;
        this.seatRepository = seatRepository;
        this.showService = showService;
        this.ticketRepository = ticketRepository;
        this.holdRepository = holdRepository;
        this.roomMapper = roomMapper;
        this.sectorValidator = sectorValidator;
    }

    @Override
    @Transactional
    public RoomDetailDto createRoom(CreateRoomDto dto) {
        LOGGER.info("Creating a new room with details: {}", dto);
        EventLocation location = eventLocationRepository.findById(dto.getEventLocationId())
            .orElseThrow(() -> new NotFoundException(
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

        roomRepository.saveAndFlush(savedRoom);
        Room persistedRoom = roomRepository.findAllWithSectorsAndSeats().stream()
            .filter(r -> r.getId().equals(savedRoom.getId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Room not found with id " + savedRoom.getId()));
        return roomMapper.roomToRoomDetailDto(persistedRoom);
    }

    @Override
    @Transactional
    public RoomDetailDto updateRoom(Long id, RoomDetailDto dto) throws ValidationException {
        LOGGER.info("Updating a room with details: {}", dto);
        if (!Objects.equals(id, dto.getId())) {
            throw new IllegalArgumentException("ID in path and payload must match");
        }

        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Room not found: " + id));

        List<SectorDto> dtoSectors = dto.getSectors();
        if (!dtoSectors.isEmpty()) {
            for (SectorDto sectorDto : dtoSectors) {
                sectorValidator.validateSector(sectorDto);
            }
        }

        room.setName(dto.getName());

        room.setEventLocation(
            eventLocationRepository.findById(dto.getEventLocationId())
                .orElseThrow(() -> new NotFoundException(
                    "EventLocation not found with id " + dto.getEventLocationId())));

        Map<Long, Sector> replacedSectors = new HashMap<>();
        List<Sector> toKeep = syncSectors(room, dtoSectors, replacedSectors);

        syncSeats(room, dto.getSeats(), replacedSectors);

        room.getSectors().removeIf(exists ->
            toKeep.stream().noneMatch(s -> Objects.equals(s.getId(), exists.getId())));

        roomRepository.saveAndFlush(room);

        return roomMapper.roomToRoomDetailDto(room);
    }

    /**
     * Replaces the type of a sector in a room with a new one based on the provided DTO.
     *
     * @param room      the Room containing the sector to replace
     * @param oldSector the existing Sector to be replaced
     * @param newDto    the DTO containing new sector details
     * @return the newly created Sector instance
     */
    private Sector replaceSectorType(Room room, Sector oldSector, SectorDto newDto) {
        LOGGER.debug("Replacing sector type for sector ID {} to new type {}", oldSector.getId(), newDto.getClass().getSimpleName());

        Sector newSector;
        if (newDto instanceof StandingSectorDto ssd) {
            StandingSector standingSector = new StandingSector();
            standingSector.setCapacity(ssd.getCapacity());
            standingSector.setPrice(ssd.getPrice());
            standingSector.setRoom(room);
            newSector = sectorRepository.save(standingSector);
        } else if (newDto instanceof StageSectorDto) {
            StageSector stageSector = new StageSector();
            stageSector.setPrice(null);
            stageSector.setRoom(room);
            newSector = sectorRepository.save(stageSector);
        } else {
            Sector normalSector = new Sector();
            normalSector.setPrice(newDto.getPrice());
            normalSector.setRoom(room);
            newSector = sectorRepository.save(normalSector);
        }

        for (Seat seat : room.getSeats()) {
            if (seat.getSector() != null && Objects.equals(seat.getSector().getId(), oldSector.getId())) {
                seat.setSector(newSector);
            }
        }

        room.getSectors().remove(oldSector);
        room.addSector(newSector);

        return newSector;
    }

    @Override
    public Sector getSectorById(Long sectorId) {
        return sectorRepository.findById(sectorId)
            .orElseThrow(() -> new NotFoundException("Sector not found with id " + sectorId));
    }

    @Override
    public Seat getSeatById(Long seatId) {
        return seatRepository.findById(seatId)
            .orElseThrow(() -> new NotFoundException("Seat not found with id " + seatId));
    }

    @Override
    @Transactional
    public RoomDetailDto getRoomById(Long id) {
        LOGGER.debug("Retrieving a room with details: {}", id);
        Room room = roomRepository.findById(id).orElseThrow(NotFoundException::new);
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
            .distinct()
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


    public Page<RoomDetailDto> getAllRoomsPaginated(Pageable pageable) {
        LOGGER.debug("Fetching all rooms paginated");
        return roomRepository.findAll(pageable)
            .map(roomMapper::roomToRoomDetailDto);
    }

    /**
     * Synchronizes the Room's seats with the given list of SeatDto: updates existing,
     * adds new ones, and removes those not present in the DTO.
     *
     * @param room            the managed Room entity
     * @param seatDtos        list of SeatDto representing desired final state
     * @param replacedSectors map of sector IDs where the sector type was changed
     */
    private void syncSeats(Room room, List<SeatDto> seatDtos, Map<Long, Sector> replacedSectors) {
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
                    throw new NotFoundException("Seat not found with id " + dto.getId());
                }
            } else {
                seat = new Seat();
                room.addSeat(seat);
            }

            seat.setRowNumber(dto.getRowNumber());
            seat.setColumnNumber(dto.getColumnNumber());
            seat.setDeleted(dto.isDeleted());

            if (dto.getSectorId() != null) {
                Sector sector = room.getSectors().stream()
                    .filter(s -> Objects.equals(s.getId(), dto.getSectorId()))
                    .findFirst()
                    .orElseGet(() -> replacedSectors.get(dto.getSectorId()));
                if (sector == null) {
                    throw new NotFoundException("Sector not found: " + dto.getSectorId());
                }
                seat.setSector(sector);
            } else {
                seat.setSector(null);
            }

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
     * @return a list of sectors that should be kept in the room
     */
    private List<Sector> syncSectors(Room room, List<SectorDto> sectorDtos, Map<Long, Sector> replacedSectors) throws ValidationException {
        LOGGER.debug("Syncing the sectors with new data: {}", sectorDtos);
        Map<Long, Sector> existing = room.getSectors().stream()
            .collect(Collectors.toMap(Sector::getId, Function.identity()));
        List<Sector> toKeep = new ArrayList<>();

        for (SectorDto sd : sectorDtos) {
            Sector sector;

            if (sd instanceof StandingSectorDto ssd) {
                if (ssd.getId() != null && existing.containsKey(ssd.getId())) {
                    Sector raw = existing.get(ssd.getId());
                    if (raw instanceof StandingSector) {
                        sector = syncStanding(existing, room, ssd);
                    } else {
                        sector = replaceSectorType(room, raw, ssd);
                        replacedSectors.put(raw.getId(), sector);
                    }
                } else {
                    sector = syncStanding(existing, room, ssd);
                }

            } else if (sd instanceof StageSectorDto stgd) {
                if (stgd.getId() != null && existing.containsKey(stgd.getId())) {
                    Sector raw = existing.get(stgd.getId());
                    if (raw instanceof StageSector) {
                        sector = syncStage(existing, room, stgd);
                    } else {
                        sector = replaceSectorType(room, raw, stgd);
                        replacedSectors.put(raw.getId(), sector);
                    }
                } else {
                    sector = syncStage(existing, room, stgd);
                }

            } else if (sd instanceof SectorDto normal) {
                if (normal.getId() != null && existing.containsKey(normal.getId())) {
                    Sector raw = existing.get(normal.getId());
                    if (raw.getClass() == Sector.class) {
                        sector = syncNormalSector(existing, room, normal);
                    } else {
                        sector = replaceSectorType(room, raw, normal);
                        replacedSectors.put(raw.getId(), sector);
                    }
                } else {
                    sector = syncNormalSector(existing, room, normal);
                }

            } else {
                throw new IllegalArgumentException("Unknown sector DTO type: " + sd.getClass());
            }

            toKeep.add(sector);
        }

        return toKeep;
    }


    /**
     * Synchronizes or creates a normal Sector based on the DTO.
     *
     * @param existing map of existing sectors by ID
     * @param room     the Room to attach new sectors to
     * @param dto      the SectorDto containing updated data
     * @return the managed Sector instance
     */
    private Sector syncNormalSector(Map<Long, Sector> existing, Room room, SectorDto dto) throws ValidationException {
        LOGGER.debug("Syncing NormalSector with details: {}", dto);
        if (dto.getId() != null && !existing.containsKey(dto.getId())) {
            throw new NotFoundException("Sector not found with id " + dto.getId());
        }
        Sector raw = dto.getId() != null ? existing.get(dto.getId()) : null;

        Sector sec;

        if (raw instanceof StandingSector || raw instanceof StageSector) {
            throw new ValidationException("Sector with id " + dto.getId() + " is not a NormalSector",
                List.of("Sector with id " + dto.getId() + " is not a NormalSector"));
        } else if (raw == null) {
            sec = new Sector();
            sec.setPrice(dto.getPrice());
            room.addSector(sec);
        } else {
            sec = raw;
            sec.setPrice(dto.getPrice());
            sec.setRoom(room);
        }

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
    private StandingSector syncStanding(Map<Long, Sector> existing, Room room, StandingSectorDto dto) throws ValidationException {
        LOGGER.debug("Syncing the StandingSector with details: {}", dto);
        if (dto.getId() != null && !existing.containsKey(dto.getId())) {
            throw new NotFoundException("StandingSector not found with id " + dto.getId());
        }
        Sector raw = dto.getId() != null ? existing.get(dto.getId()) : null;

        StandingSector sec;

        if (raw != null && !(raw instanceof StandingSector)) {
            throw new ValidationException("Sector with id " + dto.getId() + " is not a StandingSector",
                List.of("Sector with id " + dto.getId() + " is not a StandingSector"));
        } else if (raw == null) {
            sec = new StandingSector();
            sec.setPrice(dto.getPrice());
            sec.setCapacity(dto.getCapacity());
            room.addSector(sec);
        } else {
            sec = (StandingSector) raw;
            sec.setPrice(dto.getPrice());
            sec.setCapacity(dto.getCapacity());
            sec.setRoom(room);
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
    private StageSector syncStage(Map<Long, Sector> existing, Room room, StageSectorDto dto) throws ValidationException {
        LOGGER.debug("Syncing the StageSector with details: {}", dto);
        if (dto.getId() != null && !existing.containsKey(dto.getId())) {
            throw new NotFoundException("StageSector not found with id " + dto.getId());
        }
        Sector raw = dto.getId() != null ? existing.get(dto.getId()) : null;

        StageSector sec;

        if (raw != null && !(raw instanceof StageSector)) {
            throw new ValidationException("Sector with id " + dto.getId() + " is not a StageSector",
                List.of("Sector with id " + dto.getId() + " is not a StageSector"));
        } else if (raw == null) {
            sec = new StageSector();
            sec.setPrice(null);
            room.addSector(sec);
        } else {
            sec = (StageSector) raw;
            sec.setPrice(null);
            sec.setRoom(room);
        }

        return sec;
    }
}
