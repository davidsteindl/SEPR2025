package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.CheckoutRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.CreateHoldDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderGroupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.ReservationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetSeatedDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetStandingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.OrderMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.TicketMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Hold;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.SeatedSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.OrderGroup;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HoldRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderGroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.AuthenticationFacade;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import at.ac.tuwien.sepr.groupphase.backend.service.TicketService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.TicketValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketValidator ticketValidator;
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketServiceImpl.class);
    private final ShowService showService;
    private final TicketRepository ticketRepository;
    private final RoomService roomService;
    private final OrderRepository orderRepository;
    private final OrderGroupRepository orderGroupRepository;
    private final TicketMapper ticketMapper;
    private final HoldRepository holdRepository;
    private final AuthenticationFacade authFacade;
    private final OrderMapper orderMapper;

    private record TicketCreationResult(List<Ticket> tickets, int totalPrice) {
    }

    @Autowired
    public TicketServiceImpl(
        TicketValidator ticketValidator,
        ShowService showService,
        TicketRepository ticketRepository,
        RoomService roomService,
        OrderRepository orderRepository,
        OrderGroupRepository orderGroupRepository,
        TicketMapper ticketMapper,
        HoldRepository holdRepository,
        AuthenticationFacade authFacade,
        OrderMapper orderMapper) {

        this.ticketValidator = ticketValidator;
        this.showService = showService;
        this.ticketRepository = ticketRepository;
        this.roomService = roomService;
        this.orderRepository = orderRepository;
        this.orderGroupRepository = orderGroupRepository;
        this.ticketMapper = ticketMapper;
        this.holdRepository = holdRepository;
        this.authFacade = authFacade;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public OrderDto getOrderById(Long id) {
        return orderRepository.findById(id)
            .map(order -> buildOrderDto(order, order.getTickets()))
            .orElse(null);
    }

    @Override
    @Transactional
    public TicketDto getTicketById(Long id) {
        return ticketRepository.findById(id)
            .map(ticketMapper::toDto)
            .orElse(null);
    }

    @Override
    @Transactional
    public OrderDto buyTickets(TicketRequestDto request) {
        LOGGER.debug("Buy tickets request: {}", request);
        ticketValidator.validateForBuyTickets(request);
        Show show = loadShow(request.getShowId());
        Order order = initOrder(authFacade.getCurrentUserId(), OrderType.ORDER);

        var result = createTickets(order, show, request.getTargets(), TicketStatus.BOUGHT);
        finalizeOrder(order, result.tickets);
        var dto = buildOrderDto(order, result.tickets);
        dto.setTotalPrice(result.totalPrice);
        return dto;
    }

    @Override
    @Transactional
    public ReservationDto reserveTickets(TicketRequestDto request) {
        LOGGER.debug("Reserve tickets request: {}", request);
        ticketValidator.validateForReserveTickets(request);
        Show show = loadShow(request.getShowId());
        Order order = initOrder(authFacade.getCurrentUserId(), OrderType.RESERVATION);

        var result = createTickets(order, show, request.getTargets(), TicketStatus.RESERVED);
        finalizeOrder(order, result.tickets);
        return buildReservationDto(order, result.tickets, show.getDate().minusMinutes(30));
    }

    /**
     * Loads a Show by its ID or throws NotFoundException if not found.
     *
     * @param showId the ID of the show to load
     * @return the Show entity
     * @throws NotFoundException if no Show exists with the provided ID
     */
    private Show loadShow(Long showId) {
        LOGGER.debug("Loading show with id: {}", showId);
        Show show = showService.getShowById(showId);
        if (show == null) {
            throw new NotFoundException("Show with id " + showId + " not found");
        }
        return show;
    }

    /**
     * Initializes a new Order for a user with the specified type and persists it.
     *
     * @param userId the ID of the user placing the order
     * @param type the type of the order (ORDER or RESERVATION)
     * @return the persisted Order entity
     */
    private Order initOrder(Long userId, OrderType type) {
        LOGGER.debug("Initializing order for user: {}, type: {}", userId, type);
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setTickets(List.of());
        order.setUserId(userId);
        order.setOrderType(type);
        return orderRepository.save(order);
    }

    /**
     * Creates tickets for the given order and show based on target DTOs, saves them, and calculates total price.
     *
     * @param order the Order to attach tickets to
     * @param show the Show for which tickets are created
     * @param targets the list of ticket target DTOs specifying seats or quantities
     * @param status the status to apply to created tickets (BOUGHT or RESERVED)
     * @return a TicketCreationResult containing saved tickets and total price in cents
     */
    private TicketCreationResult createTickets(Order order,
                                               Show show,
                                               List<TicketTargetDto> targets,
                                               TicketStatus status) {
        LOGGER.debug("Creating tickets for order: {}, show: {}, targets: {}, status: {}", order, show, targets, status);
        List<Ticket> tickets = new ArrayList<>();
        int totalPrice = 0;

        for (TicketTargetDto target : targets) {
            if (target instanceof TicketTargetSeatedDto seated) {

                Sector raw = roomService.getSectorById(seated.getSectorId());
                // …then verify it really is a SeatedSector
                if (!(raw instanceof SeatedSector sector)) {
                    throw new IllegalArgumentException(
                        "Sector " + seated.getSectorId() + " is not a seated sector"
                    );
                }
                Seat seat = roomService.getSeatById(seated.getSeatId());

                Ticket ticket = buildTicket(order, show, sector, seat, status);
                tickets.add(ticket);
                totalPrice += (status == TicketStatus.BOUGHT ? sector.getPrice() : 0);

            } else if (target instanceof TicketTargetStandingDto standing) {
                Sector raw = roomService.getSectorById(standing.getSectorId());
                if (!(raw instanceof StandingSector sector)) {
                    throw new IllegalArgumentException(
                        "Sector " + standing.getSectorId() + " is not a standing sector"
                    );
                }

                for (int i = 0; i < standing.getQuantity(); i++) {
                    Ticket ticket = buildTicket(order, show, sector, null, status);
                    tickets.add(ticket);
                    totalPrice += (status == TicketStatus.BOUGHT ? sector.getPrice() : 0);
                }
            } else {
                throw new IllegalArgumentException("Unknown ticket target type: " + target);
            }
        }

        List<Ticket> saved = ticketRepository.saveAll(tickets);
        return new TicketCreationResult(saved, totalPrice);
    }

    /**
     * Builds a Ticket entity with the specified order, show, sector, seat, and status.
     *
     * @param order the Order to which the ticket belongs
     * @param show the Show associated with the ticket
     * @param sector the Sector (seated or standing) for the ticket
     * @param seat the Seat for seated tickets (null for standing tickets)
     * @param status the status to assign to the ticket
     * @return the constructed Ticket entity
     */
    private Ticket buildTicket(Order order,
                               Show show,
                               Sector sector,
                               Seat seat,
                               TicketStatus status) {
        LOGGER.debug("Building ticket for order: {}, show: {}, sector: {}, seat: {}, status: {}", order, show, sector, seat, status);
        Ticket ticket = new Ticket();
        ticket.setOrder(order);
        ticket.setShow(show);
        ticket.setStatus(status);
        ticket.setSector(sector);
        if (seat != null) {
            ticket.setSeat(seat);
        }
        ticket.setCreatedAt(LocalDateTime.now());
        return ticket;
    }

    /**
     * Finalizes the given Order by setting its tickets and persisting the update.
     *
     * @param order the Order to finalize
     * @param tickets the list of tickets to associate with the order
     */
    private void finalizeOrder(Order order, List<Ticket> tickets) {
        LOGGER.debug("Finalize order: {}", order);
        order.setTickets(tickets);
        orderRepository.save(order);
    }

    /**
     * Constructs an OrderDto from an Order entity and its tickets.
     *
     * @param order the Order entity to convert
     * @param tickets the list of Ticket entities associated with the order
     * @return the populated OrderDto
     */
    private OrderDto buildOrderDto(Order order, List<Ticket> tickets) {
        LOGGER.debug("Build order: {}", order);
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setOrderType(order.getOrderType());
        dto.setUserId(order.getUserId());
        dto.setTickets(tickets.stream()
            .map(ticketMapper::toDto)
            .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Constructs a ReservationDto from an Order entity, its tickets, and expiration time.
     *
     * @param order the Order entity to convert
     * @param tickets the list of Ticket entities associated with the order
     * @param expiresAt the timestamp when the reservation expires
     * @return the populated ReservationDto
     */
    private ReservationDto buildReservationDto(Order order,
                                               List<Ticket> tickets,
                                               LocalDateTime expiresAt) {
        LOGGER.debug("Building reservation DTO with expiration at {}", expiresAt);
        ReservationDto dto = new ReservationDto();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setOrderType(order.getOrderType());
        dto.setUserId(order.getUserId());
        dto.setTickets(tickets.stream()
            .map(ticketMapper::toDto)
            .collect(Collectors.toList()));
        dto.setExpiresAt(expiresAt);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto buyReservedTickets(List<Long> ticketIds) {
        LOGGER.debug("Buy reserved tickets request: {}", ticketIds);
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
        ticketValidator.validateForBuyReservedTickets(ticketIds, tickets);

        Order oldReservation = tickets.getFirst().getOrder();
        Order newOrder = processTicketTransfer(
            tickets,
            oldReservation,
            OrderType.ORDER,
            TicketStatus.BOUGHT
        );

        var saved = ticketRepository.findAllById(ticketIds);
        var dto = buildOrderDto(newOrder, saved);
        dto.setTotalPrice(calculateTotalPrice(saved));
        return dto;
    }

    @Override
    @Transactional
    public List<TicketDto> cancelReservations(List<Long> ticketIds) {
        LOGGER.debug("Cancel ticket reservations request: {}", ticketIds);
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
        ticketValidator.validateForCancelReservations(ticketIds, tickets);

        if (tickets.isEmpty()) {
            return List.of();
        }

        Order oldRes = tickets.getFirst().getOrder();
        Order cancelOrder = processTicketTransfer(
            tickets,
            oldRes,
            OrderType.CANCELLATION,
            TicketStatus.CANCELLED
        );

        return ticketRepository.findAllById(ticketIds)
            .stream()
            .map(ticketMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TicketDto> refundTickets(List<Long> ticketIds) {
        LOGGER.debug("Refund tickets request: {}", ticketIds);
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
        ticketValidator.validateForRefundTickets(ticketIds, tickets);

        if (tickets.isEmpty()) {
            return List.of();
        }

        Order original = tickets.getFirst().getOrder();
        Order refundOrder = processTicketTransfer(
            tickets,
            original,
            OrderType.REFUND,
            TicketStatus.REFUNDED
        );

        return ticketRepository.findAllById(ticketIds)
            .stream()
            .map(ticketMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Calculates the sum of prices for a list of tickets.
     *
     * @param tickets the tickets whose sector prices will be summed
     * @return the total price across all provided tickets
     */
    private int calculateTotalPrice(List<Ticket> tickets) {
        return tickets.stream()
            .mapToInt(t -> t.getSector().getPrice())
            .sum();
    }

    /**
     * Transfers tickets from an existing order to a new order of the specified type and status.
     *
     * @param tickets   the tickets to transfer
     * @param oldOrder  the original order containing these tickets
     * @param newType   the type to assign to the new order (e.g., ORDER, REFUND)
     * @param newStatus the status to set on each ticket after transfer
     * @return the newly created order containing the transferred tickets
     */
    private Order processTicketTransfer(
        List<Ticket> tickets,
        Order oldOrder,
        OrderType newType,
        TicketStatus newStatus
    ) {
        Order newOrder = initOrder(authFacade.getCurrentUserId(), newType);

        // detach and reattach
        tickets.forEach(t -> {
            oldOrder.getTickets().remove(t);
            t.setOrder(newOrder);
            t.setStatus(newStatus);
        });

        orderRepository.save(oldOrder);
        ticketRepository.saveAll(tickets);
        finalizeOrder(newOrder, tickets);
        return newOrder;
    }

    @Override
    public void createTicketHold(CreateHoldDto createHoldDto) {
        LOGGER.debug("Hold seat with id {} for show {}", createHoldDto.getSeatId(), createHoldDto.getShowId());

        ticketValidator.validateHold(createHoldDto.getShowId(), createHoldDto.getSectorId(), createHoldDto.getSeatId(), createHoldDto.getUserId());

        Hold hold = new Hold();
        hold.setShowId(createHoldDto.getShowId());
        hold.setSeatId(createHoldDto.getSeatId());
        hold.setUserId(createHoldDto.getSeatId());
        hold.setSectorId(createHoldDto.getSectorId());
        hold.setValidUntil(LocalDateTime.now().plusMinutes(30));

        holdRepository.save(hold);


    }

    /**
     * Executes the full checkout operation, including payment validation, upgrading reserved tickets to purchased status,
     * and creating new tickets if needed.
     *
     * @param dto The {@link CheckoutRequestDto} containing all data required to complete the ticket purchase
     * @return A fully populated {@link OrderGroupDto} including show details, address, total price, and ticket info
     * @throws ValidationException If credit card or address data fails validation
     */
    @Override
    @Transactional
    public OrderGroupDto checkoutTickets(CheckoutRequestDto dto) throws ValidationException {
        LOGGER.debug("Checkout ticket purchase: {}", dto);
        ticketValidator.validateCheckoutPaymentData(dto);
        ticketValidator.validateCheckoutAddress(dto);

        Long userId = authFacade.getCurrentUserId();

        OrderGroup group = new OrderGroup();
        group.setUserId(userId);
        orderGroupRepository.save(group);

        Order order = initOrderWithAddress(userId, OrderType.ORDER, dto);
        order.setOrderGroup(group);
        orderRepository.save(order);

        Show show = loadShow(dto.getShowId());

        List<Ticket> finalTickets = new ArrayList<>();
        int totalPrice = 0;

        if (dto.getReservedTicketIds() != null && !dto.getReservedTicketIds().isEmpty()) {
            List<Ticket> reservedTickets = ticketRepository.findAllById(dto.getReservedTicketIds());
            for (Ticket t : reservedTickets) {
                if (t.getStatus() != TicketStatus.RESERVED) {
                    throw new IllegalStateException("Ticket " + t.getId() + " is not reserved");
                }
                t.setStatus(TicketStatus.BOUGHT);
                t.setOrder(order);
                finalTickets.add(t);
                totalPrice += t.getSector().getPrice();
            }
        }

        if (dto.getTargets() != null && !dto.getTargets().isEmpty()) {
            var result = createTickets(order, show, dto.getTargets(), TicketStatus.BOUGHT);
            finalTickets.addAll(result.tickets);
            totalPrice += result.totalPrice;
        }

        ticketRepository.saveAll(finalTickets);
        finalizeOrder(order, finalTickets);

        OrderDto orderDto = buildOrderDto(order, finalTickets);
        orderDto.setTotalPrice(totalPrice);
        setAddressOnOrderDto(orderDto, dto);

        OrderGroupDto groupDto = new OrderGroupDto();
        groupDto.setId(group.getId());
        groupDto.setOrders(List.of(orderDto));
        groupDto.setShowName(show.getName());
        groupDto.setShowDate(show.getDate());
        groupDto.setLocationName(show.getEvent().getLocation().getName());
        groupDto.setTotalPrice(totalPrice);

        return groupDto;
    }


    /**
     * Copies address information from the {@link CheckoutRequestDto} into the resulting {@link OrderDto}.
     *
     * @param dto The target order DTO to populate with address fields
     * @param request The source request containing address input from the user
     */
    private void setAddressOnOrderDto(OrderDto dto, CheckoutRequestDto request) {
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setStreet(request.getStreet());
        dto.setHousenumber(request.getHousenumber());
        dto.setCity(request.getCity());
        dto.setCountry(request.getCountry());
        dto.setPostalCode(request.getPostalCode());
    }

    /**
     * Initializes a new {@link Order} with basic metadata (user, type, timestamp) and full billing address data.
     *
     * @param userId The ID of the currently authenticated user
     * @param type The type of the order (typically {@code ORDER})
     * @param dto The checkout data containing address information
     * @return A newly constructed {@link Order} (not yet persisted)
     */
    private Order initOrderWithAddress(Long userId, OrderType type, CheckoutRequestDto dto) {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setTickets(List.of());
        order.setUserId(userId);
        order.setOrderType(type);

        order.setFirstName(dto.getFirstName());
        order.setLastName(dto.getLastName());
        order.setStreet(dto.getStreet());
        order.setHousenumber(dto.getHousenumber());
        order.setCity(dto.getCity());
        order.setCountry(dto.getCountry());
        order.setPostalCode(dto.getPostalCode());

        return order;
    }

    @Override
    @Transactional
    public ReservationDto reserveTicketsGrouped(TicketRequestDto request) {
        LOGGER.debug("Grouped reservation request: {}", request);
        ticketValidator.validateForReserveTickets(request);

        Long userId = authFacade.getCurrentUserId();

        OrderGroup group = new OrderGroup();
        group.setUserId(userId);
        orderGroupRepository.save(group);

        Order order = initOrder(userId, OrderType.RESERVATION);
        order.setOrderGroup(group);
        orderRepository.save(order);

        Show show = loadShow(request.getShowId());

        var result = createTickets(order, show, request.getTargets(), TicketStatus.RESERVED);
        ticketRepository.saveAll(result.tickets);
        finalizeOrder(order, result.tickets);

        return buildReservationDto(order, result.tickets, show.getDate().minusMinutes(30));
    }



    @Override
    @Transactional
    public Page<OrderDto> getOrdersForUser(Long userId, OrderType orderType, boolean past, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Long> orderIdsPage = orderRepository.findOrderIdsByTypeAndPast(userId, orderType, past, now, pageable);
        List<Order> fullOrders = orderRepository.findAllWithDetailsByIdIn(orderIdsPage.getContent());
        List<OrderDto> orderDtos = orderMapper.toDto(fullOrders);

        return new PageImpl<>(orderDtos, pageable, orderIdsPage.getTotalElements());
    }

    @Override
    @Transactional
    public OrderDto getOrderWithTicketsById(Long orderId) {
        return orderRepository.findByIdWithDetails(orderId)
            .map(orderMapper::toDto)
            .orElseThrow(() -> new NotFoundException("Order with ID " + orderId + " not found"));
    }

}
