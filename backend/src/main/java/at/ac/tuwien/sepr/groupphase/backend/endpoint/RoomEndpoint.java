package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.CreateRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.RoomDetailDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("api/v1/rooms")
public class RoomEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @PostMapping
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new Room Layout", security = @SecurityRequirement(name = "apiKey"))
    public RoomDetailDto createRoom(@RequestBody @Valid CreateRoomDto createRoomDto) {
        LOGGER.info("POST /api/v1/rooms");
        // TODO: process POST request
        return null;
    }

    @PutMapping("/{id}")
    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Edit a Room Layout", security = @SecurityRequirement(name = "apiKey"))
    public void editRoom(@PathVariable Long id, @RequestBody @Valid RoomDetailDto roomDetailDto) {
        // TODO: process PUT request

    }

}
