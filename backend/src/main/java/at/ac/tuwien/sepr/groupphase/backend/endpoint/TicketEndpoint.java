package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderGroupType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.CheckoutRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.CreateHoldDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
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
        @RequestBody @Valid TicketRequestDto ticketRequestDto) {
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

    @PostMapping("/checkout")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Checkout: buy reserved/new tickets with adresse and payment", security = @SecurityRequirement(name = "apiKey"))
    public OrderGroupDto checkoutTickets(@RequestBody @Valid CheckoutRequestDto dto) throws ValidationException {
        LOGGER.info("POST /api/v1/tickets/checkout with request {}", dto);
        return ticketService.checkoutTickets(dto);
    }

    @PostMapping("/reserve-grouped")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reserves tickets in a new OrderGroup", security = @SecurityRequirement(name = "apiKey"))
    public ReservationDto reserveTicketsGrouped(@RequestBody @Valid TicketRequestDto ticketRequestDto) {
        LOGGER.info("POST /api/v1/tickets/reserve-grouped with request {}", ticketRequestDto);
        return ticketService.reserveTicketsGrouped(ticketRequestDto);
    }


    @PostMapping("/refund-grouped")
    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Refund selected tickets and split remaining into a new order in the same OrderGroup", security = @SecurityRequirement(name = "apiKey"))
    public List<TicketDto> refundTicketsGrouped(@RequestBody List<Long> ticketIds) {
        LOGGER.info("POST /api/v1/tickets/refund-grouped with tickets {}", ticketIds);
        return ticketService.refundTicketsGroup(ticketIds);
    }


    @GetMapping("/orders/upcoming")
    @Secured("ROLE_USER")
    @Operation(summary = "Get purchased upcoming orders", security = @SecurityRequirement(name = "apiKey"))
    public Page<OrderDto> getUpcomingPurchasedOrders(Pageable pageable) {
        Long userId = authenticationFacade.getCurrentUserId();
        LOGGER.info("GET /api/v1/tickets/orders/upcoming by user {}", userId);
        return ticketService.getOrdersForUser(userId, OrderType.ORDER, false, pageable);
    }

    @GetMapping("/orders/reservations")
    @Secured("ROLE_USER")
    @Operation(summary = "Get active reservations", security = @SecurityRequirement(name = "apiKey"))
    public Page<OrderDto> getUpcomingReservations(Pageable pageable) {
        Long userId = authenticationFacade.getCurrentUserId();
        LOGGER.info("GET /api/v1/tickets/orders/reservations by user {}", userId);
        return ticketService.getOrdersForUser(userId, OrderType.RESERVATION, false, pageable);
    }

    @GetMapping("/orders/past")
    @Secured("ROLE_USER")
    @Operation(summary = "Get past purchased orders", security = @SecurityRequirement(name = "apiKey"))
    public Page<OrderDto> getPastOrders(Pageable pageable) {
        Long userId = authenticationFacade.getCurrentUserId();
        LOGGER.info("GET /api/v1/tickets/orders/past by user {}", userId);
        return ticketService.getOrdersForUser(userId, OrderType.ORDER, true, pageable);
    }

    @GetMapping("/orders/refunded")
    @Secured("ROLE_USER")
    @Operation(summary = "Get refunded orders", security = @SecurityRequirement(name = "apiKey"))
    public Page<OrderDto> getRefundedOrders(Pageable pageable) {
        Long userId = authenticationFacade.getCurrentUserId();
        LOGGER.info("GET /api/v1/tickets/orders/refunded by user {}", userId);
        return ticketService.getOrdersForUser(userId, OrderType.REFUND, false, pageable);
    }

    @GetMapping("/orders/{orderId}/with-tickets")
    @Secured("ROLE_USER")
    @Operation(summary = "Get full order including tickets", security = @SecurityRequirement(name = "apiKey"))
    public OrderDto getOrderWithTickets(@PathVariable("orderId") Long orderId) {
        LOGGER.info("GET /api/v1/tickets/orders/{}/with-tickets by user {}", orderId, authenticationFacade.getCurrentUserId());
        return ticketService.getOrderWithTicketsById(orderId);
    }

    @GetMapping("/ordergroups")
    @Secured("ROLE_USER")
    @Operation(
        summary = "Get grouped orders (reservations, purchases, or past orders)",
        description = "Returns a paginated list of order groups categorized by status and show date",
        security = @SecurityRequirement(name = "apiKey")
    )
    public Page<OrderGroupDto> getGroupedOrders(
        @org.springframework.web.bind.annotation.RequestParam("category") OrderGroupType category,
        Pageable pageable
    ) {
        Long userId = authenticationFacade.getCurrentUserId();
        LOGGER.info("GET /api/v1/tickets/ordergroups?category={} by user {}", category, userId);
        return ticketService.getOrderGroupsForUser(category, pageable);
    }

    @GetMapping("/ordergroups/{id}")
    @Secured("ROLE_USER")
    @Operation(
        summary = "Get detailed view of an OrderGroup including all orders and tickets",
        security = @SecurityRequirement(name = "apiKey")
    )
    public OrderGroupDto getOrderGroupDetails(@PathVariable Long id) {
        LOGGER.info("GET /api/v1/tickets/ordergroups/{} by user {}", id, authenticationFacade.getCurrentUserId());
        return ticketService.getOrderGroupDetails(id);
    }


}
