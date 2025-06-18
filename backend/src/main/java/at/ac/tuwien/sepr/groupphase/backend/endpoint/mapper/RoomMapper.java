package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SectorMapper.class, SeatMapper.class})
public interface RoomMapper {

    @Mapping(target = "eventLocationId", source = "eventLocation.id")
    RoomDetailDto roomToRoomDetailDto(Room room);

    @Mapping(target = "eventLocation", source = "eventLocationId", qualifiedByName = "mapLocationIdToEventLocation")
    Room roomDetailDtoToRoom(RoomDetailDto dto);

    List<RoomDetailDto> roomsToRoomDetailDtos(List<Room> rooms);

    @Named("mapLocationIdToEventLocation")
    default EventLocation mapLocationIdToEventLocation(Long id) {
        if (id == null) {
            return null;
        }
        EventLocation location = new EventLocation();
        location.setId(id);
        return location;
    }
}
