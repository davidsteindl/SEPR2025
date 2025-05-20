package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.ReservationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetSeatedDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetStandingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.TicketMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HoldRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.RoomService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import at.ac.tuwien.sepr.groupphase.backend.service.TicketService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.TicketValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final TicketMapper ticketMapper;
    private final HoldRepository holdRepository;

    @Autowired
    public TicketServiceImpl(TicketValidator ticketValidator, ShowService showService, TicketRepository ticketRepository, RoomService roomService, OrderRepository orderRepository, TicketMapper ticketMapper, HoldRepository holdRepository) {
        this.ticketValidator = ticketValidator;
        this.showService = showService;
        this.ticketRepository = ticketRepository;
        this.roomService = roomService;
        this.orderRepository = orderRepository;
        this.ticketMapper = ticketMapper;
        this.holdRepository = holdRepository;
    }

    @Override
    public OrderDto getOrderById(Long id) {
        return null;
    }

    @Override
    public TicketDto getTicketById(Long id) {
        return null;
    }

    @Override
    @Transactional
    public OrderDto buyTickets(TicketRequestDto ticketRequestDto) {
        LOGGER.debug("Buy tickets request: {}", ticketRequestDto);
        ticketValidator.validateForBuyTickets(ticketRequestDto);

        // Load Show
        Show show = showService.getShowById(ticketRequestDto.getShowId());
        if (show == null) {
            throw new NotFoundException("Show with id " + ticketRequestDto.getShowId() + " not found");
        }

        // Save new order
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setTickets(List.of());
        order.setUserId(null); // TODO: Find out how to get the userId
        order.setOrderType(OrderType.ORDER);
        order = orderRepository.save(order);

        List<Ticket> created = new ArrayList<>();
        int totalPriceInCents = 0;
        for (TicketTargetDto targetDto : ticketRequestDto.getTargets()) {
            if (targetDto instanceof TicketTargetSeatedDto seated) {
                SeatedSector sector = (SeatedSector) roomService.getSectorById(seated.getSectorId());
                Seat seat           = roomService.getSeatById(seated.getSeatId());

                Ticket ticket = new Ticket();
                ticket.setOrder(order);
                ticket.setShow(show);
                ticket.setStatus(TicketStatus.BOUGHT);
                ticket.setSector(sector);
                ticket.setSeat(seat);
                ticket.setCreatedAt(LocalDateTime.now());

                created.add(ticket);

                int priceCents      = sector.getPrice();
                totalPriceInCents += priceCents;

            } else if (targetDto instanceof TicketTargetStandingDto standing) {
                StandingSector sector = (StandingSector) roomService.getSectorById(standing.getSectorId());
                int priceCents      = sector.getPrice();

                for (int i = 0; i < standing.getQuantity(); i++) {
                    Ticket ticket = new Ticket();
                    ticket.setOrder(order);
                    ticket.setShow(show);
                    ticket.setStatus(TicketStatus.BOUGHT);
                    ticket.setSector(sector);
                    ticket.setCreatedAt(LocalDateTime.now());

                    created.add(ticket);
                    totalPriceInCents += priceCents;
                }
            }
        }

        // Save all tickets in batch
        created = ticketRepository.saveAll(created);


        order.setTickets(created);
        orderRepository.save(order);

        // Map entities to DTOs
        List<TicketDto> ticketDtos = created.stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setOrderType(order.getOrderType());
        dto.setTickets(ticketDtos);

        return dto;
    }

    @Override
    public ReservationDto reserveTickets(TicketRequestDto ticketRequestDto) {
        LOGGER.debug("Reserve tickets request: {}", ticketRequestDto);
        return null;
    }

    @Override
    public OrderDto buyReservedTickets(List<Long> ticketIds) {
        LOGGER.debug("Buy reserved tickets request: {}", ticketIds);
        return null;
    }

    @Override
    public List<TicketDto> cancelReservations(List<Long> ticketIds) {
        LOGGER.debug("Cancel ticket reservations request: {}", ticketIds);
        return List.of();

    }

    @Override
    public List<TicketDto> refundTickets(List<Long> ticketIds) {
        LOGGER.debug("Refund tickets request: {}", ticketIds);
        return List.of();
    }

    @Override
    public void holdSeat(Long showId, Long sectorId, Long seatId, Long userId) {
        LOGGER.debug("Hold seat with id {} for show {}", seatId, showId);

        ticketValidator.validateHold(showId, sectorId, seatId, userId);

        Hold hold = new Hold();
        hold.setShowId(showId);
        hold.setSeatId(seatId);
        hold.setUserId(seatId);
        hold.setSectorId(sectorId);
        hold.setValidUntil(LocalDateTime.now().plusMinutes(30));

        holdRepository.save(hold);


    }
}
