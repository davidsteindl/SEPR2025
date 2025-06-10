package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "sectorId", source = "sector.id")
    SeatDto seatToSeatDto(Seat seat);

    List<SeatDto> seatListToSeatDtoList(List<Seat> seats);

    @Mapping(target = "room", source = "roomId", qualifiedByName = "mapRoomIdToRoom")
    @Mapping(target = "sector", source = "sectorId", qualifiedByName = "mapSectorIdToSector")
    Seat seatDtoToSeat(SeatDto seatDto);

    List<Seat> seatDtoListToSeatList(List<SeatDto> seatDtos);

    @Named("mapRoomIdToRoom")
    default Room mapRoomIdToRoom(Long roomId) {
        if (roomId == null) {
            return null;
        }
        Room room = new Room();
        room.setId(roomId);
        return room;
    }

    @Named("mapSectorIdToSector")
    default Sector mapSectorIdToSector(Long sectorId) {
        if (sectorId == null) {
            return null;
        }
        Sector sector = new Sector();
        sector.setId(sectorId);
        return sector;
    }
}
