package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.repository.SectorRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType.CANCELLATION;
import static at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType.ORDER;
import static at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType.REFUND;
import static at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType.RESERVATION;
import static at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus.BOUGHT;
import static at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus.CANCELLED;
import static at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus.REFUNDED;
import static at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus.RESERVED;

@Profile("generateData")
@DependsOn("userDataGenerator")
@Component
public class OrderDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final SectorRepository sectorRepository;

    public OrderDataGenerator(OrderRepository orderRepository,
                              TicketRepository ticketRepository,
                              UserRepository userRepository,
                              ShowRepository showRepository,
                              SectorRepository sectorRepository) {
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.sectorRepository = sectorRepository;
    }

    @PostConstruct
    public void generateOrders() {
        LOGGER.debug("Generating test orders...");

        List<ApplicationUser> users = userRepository.findAll();
        List<Show> shows = showRepository.findAll();
        List<Sector> sectors = sectorRepository.findAll();
        Random random = new Random();

        if (users.isEmpty() || shows.isEmpty() || sectors.isEmpty()) {
            LOGGER.warn("No shows, users or sectors available, cannot generate tickets");
            return;
        }

        LOGGER.debug("Generating {} orders of type {}", 1000, ORDER);
        generateOrdersOfType(ORDER, BOUGHT, 1000, users, shows, sectors, random);

        LOGGER.debug("Generating {} orders of type {}", 250, RESERVATION);
        generateOrdersOfType(RESERVATION, RESERVED, 250, users, shows, sectors, random);

        LOGGER.debug("Generating {} orders of type {}", 250, REFUND);
        generateOrdersOfType(REFUND, REFUNDED, 250, users, shows, sectors, random);

        LOGGER.debug("Generating {} orders of type {}", 75, CANCELLATION);
        generateOrdersOfType(CANCELLATION, CANCELLED, 75, users, shows, sectors, random);

        LOGGER.debug("Created {} sales, {} reservations, {} refunded tickets, {} cancelled tickets across {} users",
            1000, 250, 250, 75, users.size());
    }

    private void generateOrdersOfType(OrderType orderType,
                                      TicketStatus ticketStatus,
                                      int count,
                                      List<ApplicationUser> users,
                                      List<Show> shows,
                                      List<Sector> sectors,
                                      Random random) {
        for (int i = 0; i < count; i++) {
            ApplicationUser user = users.get(random.nextInt(users.size()));
            LOGGER.trace("Selected user {} for new order", user.getId());

            LocalDateTime createdAt = randomPastDateTime(random);

            Order order = new Order();
            order.setUserId(user.getId());
            order.setOrderType(orderType);
            order.setCreatedAt(createdAt);
            order = orderRepository.save(order);
            LOGGER.debug("Saved Order id={} type={} for user={} at {}", order.getId(), orderType, user.getId(), createdAt);

            int ticketCount = 2 + random.nextInt(3);

            Show show = shows.get(random.nextInt(shows.size()));
            LOGGER.debug("Assigning Show id={} to Order id={} (will create {} tickets)", show.getId(), order.getId(), ticketCount);

            for (int j = 0; j < ticketCount; j++) {
                Ticket ticket = new Ticket();
                ticket.setOrder(order);
                ticket.setShow(show);
                ticket.setSector(sectors.get(random.nextInt(sectors.size())));
                ticket.setCreatedAt(createdAt);
                ticket.setStatus(ticketStatus);
                ticketRepository.save(ticket);
                LOGGER.trace("Saved Ticket id={} for Order id={} on Show id={} with status={}", ticket.getId(), order.getId(), show.getId(), ticketStatus);
            }
        }
        LOGGER.debug("Exiting generateOrdersOfType(): orderType={} (created {} orders)", orderType, count);
    }

    private LocalDateTime randomPastDateTime(Random random) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now
            .minusDays(random.nextInt(180))
            .minusHours(random.nextInt(24))
            .minusMinutes(random.nextInt(60))
            .minusSeconds(random.nextInt(60));
        LOGGER.trace("Generated randomPastDateTime: {}", past);
        return past;
    }

    private LocalDateTime randomFutureDateTime(Random random) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now
            .plusDays(random.nextInt(180))
            .plusHours(random.nextInt(24))
            .plusMinutes(random.nextInt(60))
            .plusSeconds(random.nextInt(60));
        LOGGER.trace("Generated randomFutureDateTime: {}", future);
        return future;
    }

}
