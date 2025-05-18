package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.SeatedSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;

public interface RoomService {

    /**
     * Creates a new room layout with the specified initial configuration.
     * This includes the number of sectors, rows, and seats, as well as the orientation.
     * After creation, the client may further modify the layout using {@link #updateRoom(Long, RoomDetailDto)}.
     *
     * @param createRoomDto the DTO containing the initial room configuration
     * @return a DTO representing the created room, including generated identifiers and defaults
     */
    RoomDetailDto createRoom(CreateRoomDto createRoomDto);


    /**
     * Updates an existing room layout with the given details.
     * This will overwrite the current configuration of the room identified by {@code id}.
     *
     * @param id the unique identifier of the room to update
     * @param roomDetailDto the DTO containing the updated room layout and properties
     */
    RoomDetailDto updateRoom(Long id, RoomDetailDto roomDetailDto);

    Sector getSectorById(Long sectorId);

    Seat getSeatById(Long seatId);
}
