package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TransactionStatus;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.PaymentCallbackDataDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.PaymentResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.PaymentSessionDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.ReservationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetSeatedDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetStandingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.SeatedSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.PaymentSession;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.PaymentSessionRepository;
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

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketValidator ticketValidator;
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketServiceImpl.class);
    private final ShowService showService;
    private final TicketRepository ticketRepository;
    private final RoomService roomService;
    private final OrderRepository orderRepository;
    private final PaymentSessionRepository paymentSessionRepository;

    @Autowired
    public TicketServiceImpl(TicketValidator ticketValidator, ShowService showService, TicketRepository ticketRepository, RoomService roomService, OrderRepository orderRepository, PaymentSessionRepository paymentSessionRepository) {
        this.ticketValidator = ticketValidator;
        this.showService = showService;
        this.ticketRepository = ticketRepository;
        this.roomService = roomService;
        this.orderRepository = orderRepository;
        this.paymentSessionRepository = paymentSessionRepository;
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
    public PaymentSessionDto buyTickets(TicketRequestDto ticketRequestDto) {
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
                ticket.setStatus(TicketStatus.PAYMENT_PENDING);
                ticket.setSector(sector);
                ticket.setSeat(seat);

                created.add(ticket);

                int priceCents      = sector.getPrice();
                totalPriceInCents += priceCents;

            } else if (targetDto instanceof TicketTargetStandingDto standing) {
                StandingSector sector = (StandingSector) roomService.getSectorById(standing.getSectorId());
                int unitPrice         = sector.getPrice();

                for (int i = 0; i < standing.getQuantity(); i++) {
                    Ticket ticket = new Ticket();
                    ticket.setOrder(order);
                    ticket.setShow(show);
                    ticket.setStatus(TicketStatus.PAYMENT_PENDING);
                    ticket.setSector(sector);

                    created.add(ticket);
                    totalPriceInCents += unitPrice;
                }
            }
        }

        // Save all tickets in batch
        created = ticketRepository.saveAll(created);


        order.setTickets(created);
        orderRepository.save(order);


        PaymentSession session = new PaymentSession();
        session.setOrder(order);
        session.setTickets(created);
        session.setTransactionStatus(TransactionStatus.PENDING);
        session.setCreatedAt(LocalDateTime.now());
        session.setTotalPrice(totalPriceInCents);

        session = paymentSessionRepository.save(session);

        PaymentSessionDto dto = new PaymentSessionDto();
        dto.setPaymentUrl("localhost:8080"); // TODO: we need the frontend url for this
        dto.setSessionId(session.getId());
        dto.setTarget(ticketRequestDto);
        dto.setTotalPrice(totalPriceInCents);
        return dto;
    }

    @Override
    public ReservationDto reserveTickets(TicketRequestDto ticketRequestDto) {
        return null;
    }

    @Override
    public PaymentSessionDto buyReservedTickets(Long reservationId, List<Long> ticketIds) {
        return null;
    }

    @Override
    public List<TicketDto> cancelReservations(List<Long> ticketIds) {
        return List.of();
    }

    @Override
    public List<TicketDto> refundTickets(List<Long> ticketIds) {
        return List.of();
    }

    @Override
    public PaymentResultDto completePurchase(Long sessionId, PaymentCallbackDataDto paymentCallbackDataDto) {
        return null;
    }
}
