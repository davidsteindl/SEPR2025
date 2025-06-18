package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatUsageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.StandingSectorUsageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
    RoomDetailDto updateRoom(Long id, RoomDetailDto roomDetailDto) throws ValidationException;


    /**
     * Returns the sector with the given ID.
     *
     * @param sectorId the ID of the sector to retrieve
     * @return the sector with the given ID, or null if no such sector exists
     */
    Sector getSectorById(Long sectorId);

    /**
     * Returns the seat with the given ID.
     *
     * @param seatId the ID of the seat to retrieve
     * @return the seat with the given ID, or null if no such seat exists
     */
    Seat getSeatById(Long seatId);

    /**
     * Returns the room with the given ID.
     *
     * @param id the ID of the room to retrieve
     * @return the room with the given ID, or null if no such room exists
     */
    RoomDetailDto getRoomById(Long id);


    /**
     * Retrieves the seating and standing occupancy information for the room assigned to the specified show.
     *
     * <p>
     * Returns a {@link RoomDetailDto} containing:
     * <ul>
     *   <li>a list of {@link SeatUsageDto} entries, each indicating whether a particular seat is available;</li>
     *   <li>a list of {@link StandingSectorUsageDto} entries, each indicating the total capacity and the remaining available capacity for a standing sector.</li>
     * </ul>
     *
     * @param showId the unique identifier of the show whose room usage is to be queried
     * @return a {@code RoomUsageDto} representing the current availability status of all seats and standing sectors
     * @throws NotFoundException if no show with the given {@code showId} exists, or if the show has no associated room
     */
    RoomDetailDto getRoomUsageForShow(Long showId);


    /**
     * Retrieves all rooms in the system.
     *
     * @return a list of {@link RoomDetailDto} representing all rooms
     */
    List<RoomDetailDto> getAllRooms();

    /**
     * Retrieves all rooms paginated
     *
     * @return a page of all rooms
     */
    Page<RoomDetailDto> getAllRoomsPaginated(Pageable pageable);
}
