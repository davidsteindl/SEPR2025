package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderGroupType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.CreateHoldDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderGroupDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderGroupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.ReservationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.security.AuthenticationFacade;
import at.ac.tuwien.sepr.groupphase.backend.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("api/v1/tickets")
public class TicketEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final TicketService ticketService;
    private final AuthenticationFacade authenticationFacade;

    @Autowired
    public TicketEndpoint(TicketService ticketService, AuthenticationFacade authenticationFacade) {
        this.ticketService = ticketService;
        this.authenticationFacade = authenticationFacade;
    }

    @PostMapping("/buy")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initiate ticket purchase", security = @SecurityRequirement(name = "apiKey"))
    public OrderDto buyTickets(
        @RequestBody @Valid TicketRequestDto ticketRequestDto) throws ValidationException {
        LOGGER.info("POST /api/v1/tickets/buy with request {}", ticketRequestDto);
        return ticketService.buyTickets(ticketRequestDto);
    }

    @PostMapping("/reserve")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reserve tickets", security = @SecurityRequirement(name = "apiKey"))
    public ReservationDto reserveTickets(
        @RequestBody @Valid TicketRequestDto ticketRequestDto) {
        LOGGER.info("POST /api/v1/tickets/reserve with request {}", ticketRequestDto);
        return ticketService.reserveTickets(ticketRequestDto);
    }

    @PostMapping("/reservations/{reservationId}/buy")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Purchase previously reserved tickets", security = @SecurityRequirement(name = "apiKey"))
    public OrderDto buyReservedTickets(
        @PathVariable("reservationId") Long reservationId,
        @RequestBody List<Long> ticketIds) {
        LOGGER.info("POST /api/v1/tickets/reservations/{}/buy with tickets {}", reservationId, ticketIds);
        return ticketService.buyReservedTickets(ticketIds);
    }

    @PostMapping("/cancel-reservations")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cancel ticket reservations", security = @SecurityRequirement(name = "apiKey"))
    public List<TicketDto> cancelReservations(
        @RequestBody List<Long> ticketIds) {
        LOGGER.info("POST /api/v1/tickets/cancel-reservations with tickets {}", ticketIds);
        return ticketService.cancelReservations(ticketIds);
    }

    @PostMapping("/refund")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Refund purchased tickets", security = @SecurityRequirement(name = "apiKey"))
    public List<TicketDto> refundTickets(
        @RequestBody List<Long> ticketIds) {
        LOGGER.info("POST /api/v1/tickets/refund with tickets {}", ticketIds);
        return ticketService.refundTickets(ticketIds);
    }

    @PostMapping("/holds")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a ticket hold", security = @SecurityRequirement(name = "apiKey"))
    public void createTicketHold(@RequestBody @Valid CreateHoldDto createHoldDto) {
        LOGGER.info("POST /api/v1/tickets/holds with request {}", createHoldDto);
        ticketService.createTicketHold(createHoldDto);
    }


    @GetMapping("/order-groups")
    @Secured("ROLE_USER")
    @Operation(summary = "Get order groups", security = @SecurityRequirement(name = "apiKey"))
    public Page<OrderGroupDto> getOrderGroupsByCategory(
        @RequestParam(name = "isReservation") boolean isReservation,
        @RequestParam(name = "past") boolean past,
        Pageable pageable
    ) {
        LOGGER.info("GET /api/v1/tickets/order-groups?isReservation={}&past={}", isReservation, past);
        return ticketService.getOrderGroupsByCategory(isReservation, past, pageable);
    }

    @GetMapping("/order-groups/{id}")
    @Secured("ROLE_USER")
    @Operation(summary = "Get order group details", security = @SecurityRequirement(name = "apiKey"))
    public OrderGroupDetailDto getOrderGroupDetails(@PathVariable("id") Long id) {
        return ticketService.getOrderGroupDetails(id);
    }


}
