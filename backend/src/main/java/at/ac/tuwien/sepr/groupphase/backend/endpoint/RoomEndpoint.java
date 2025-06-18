package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import java.lang.invoke.MethodHandles;

import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room.RoomDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/rooms")
public class RoomEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private RoomService roomservice;

    @Autowired
    public RoomEndpoint(RoomService roomservice) {
        this.roomservice = roomservice;
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new Room Layout", security = @SecurityRequirement(name = "apiKey"))
    public RoomDetailDto createRoom(@RequestBody @Valid CreateRoomDto createRoomDto) {
        LOGGER.info("POST /api/v1/rooms");
        return roomservice.createRoom(createRoomDto);
    }

    @PutMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Edit a Room Layout", security = @SecurityRequirement(name = "apiKey"))
    public RoomDetailDto editRoom(@PathVariable("id") Long id, @RequestBody @Valid RoomDetailDto roomDetailDto) throws ValidationException {
        LOGGER.info("PUT /api/v1/rooms/{}", id);
        return roomservice.updateRoom(id, roomDetailDto);
    }

    @GetMapping("/{id}")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get room by id", security = @SecurityRequirement(name = "apiKey"))
    public RoomDetailDto getRoomById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/rooms/{}", id);
        return roomservice.getRoomById(id);
    }

    @GetMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all rooms", security = @SecurityRequirement(name = "apiKey"))
    public List<RoomDetailDto> getAllRooms() {
        LOGGER.info("GET /api/v1/rooms");
        return roomservice.getAllRooms();
    }

    @GetMapping("/paginated")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all rooms paginated", security = @SecurityRequirement(name = "apiKey"))
    public Page<RoomDetailDto> getAllRoomsPaginated(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        LOGGER.info("GET /api/v1/rooms/page?page={}&size={}", page, size);
        Pageable sorted = PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Order.asc("eventLocation.name").ignoreCase(),
                Sort.Order.asc("name").ignoreCase()
            )
        );
        return roomservice.getAllRoomsPaginated(sorted);
    }
}
