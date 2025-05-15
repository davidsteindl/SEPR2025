package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import org.springframework.stereotype.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatedSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.SeatedSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class RoomServiceImpl implements RoomService {

    private final EventLocationRepository eventLocationRepository;
    private final RoomRepository roomRepository;

    public RoomServiceImpl(EventLocationRepository eventLocationRepository, RoomRepository roomRepository) {
        this.eventLocationRepository = eventLocationRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomDetailDto createRoom(CreateRoomDto createRoomDto) {

        EventLocation eventLocation = eventLocationRepository.findById(createRoomDto.getEventLocationId())
            .orElseThrow(() -> new EntityNotFoundException(
                "EventLocation not found with id " + createRoomDto.getEventLocationId()));

        Room room = createRoomDtoToRoomEntity(createRoomDto, eventLocation);

        Room saved = roomRepository.save(room);

        List<SectorDto> dtoSectors = saved.getSectors().stream()
            .map(sec -> {
                if (sec instanceof SeatedSector seated) {
                    List<SeatDto> seatDtos = seated.getSeats().stream()
                        .map(seat -> SeatDto.SeatDtoBuilder.aSeatDto()
                            .id(seat.getId())
                            .rowNumber(seat.getRowNumber())
                            .columnNumber(seat.getColumnNumber())
                            .deleted(seat.isDeleted())
                            .build())
                        .collect(Collectors.toList());

                    return SeatedSectorDto.SeatedSectorDtoBuilder.aSeatedSectorDto()
                        .id(seated.getId())
                        .price(seated.getPrice())
                        .rows(seatDtos)
                        .build();
                } else if (sec instanceof StandingSector standing) {
                    return StandingSectorDto.StandingSectorDtoBuilder.aStandingSectorDto()
                        .id(standing.getId())
                        .price(standing.getPrice())
                        .capacity(standing.getCapacity())
                        .build();
                } else {
                    throw new IllegalArgumentException("Unknown sector type: " + sec.getClass());
                }
            })
            .collect(Collectors.toList());

        return RoomDetailDto.RoomDetailDtoBuilder.aRoomDetailDto()
            .id(saved.getId())
            .name(saved.getName())
            .sectors(dtoSectors)
            .isHorizontal(saved.isHorizontal())
            .build();
    }

    private static Room createRoomDtoToRoomEntity(CreateRoomDto createRoomDto, EventLocation eventLocation) {
        Room room = new Room();
        room.setName(createRoomDto.getName());
        room.setHorizontal(createRoomDto.isHorizontal());
        room.setEventLocation(eventLocation);


        for (int s = 0; s < createRoomDto.getNumberOfSectors(); s++) {
            SeatedSector sector = new SeatedSector();
            sector.setPrice(0);

            for (int row = 1; row <= createRoomDto.getRowsPerSector(); row++) {
                for (int col = 1; col <= createRoomDto.getSeatsPerRow(); col++) {
                    Seat seat = new Seat();
                    seat.setRowNumber(row);
                    seat.setColumnNumber(col);
                    seat.setDeleted(false);
                    sector.addSeat(seat);
                }
            }
            room.addSector(sector);
        }
        return room;
    }

    @Override
    public void updateRoom(Long id, RoomDetailDto roomDetailDto) {
        throw new UnsupportedOperationException("Unimplemented method 'updateRoom'");
    }
}
