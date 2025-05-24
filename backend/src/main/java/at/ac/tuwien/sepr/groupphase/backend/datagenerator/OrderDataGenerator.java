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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Profile("generateData")
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

        Optional<ApplicationUser> optionalUser = Optional.ofNullable(userRepository.findByEmail("user@email.com"));
        Optional<ApplicationUser> optionalAdmin = Optional.ofNullable(userRepository.findByEmail("admin@email.com"));

        List<Show> shows = showRepository.findAll();
        List<Sector> sectors = sectorRepository.findAll();
        Random random = new Random();

        if (shows.isEmpty() || sectors.isEmpty()) {
            LOGGER.warn("No shows or sectors available, cannot generate tickets");
            return;
        }

        if (optionalUser.isPresent()) {
            generateOrdersForUser(optionalUser.get(), shows, sectors, random, "user");
        } else {
            LOGGER.warn("No user with email 'user@email.com' found");
        }

        if (optionalAdmin.isPresent()) {
            generateOrdersForUser(optionalAdmin.get(), shows, sectors, random, "admin");
        } else {
            LOGGER.warn("No admin with email 'admin@email.com' found");
        }
    }

    private OrderType randomOrderType(Random random) {
        return switch (random.nextInt(3)) {
            case 0 -> OrderType.ORDER;
            case 1 -> OrderType.RESERVATION;
            case 2 -> OrderType.REFUND;
            default -> OrderType.ORDER;
        };
    }

    private void generateOrdersForUser(ApplicationUser user, List<Show> shows, List<Sector> sectors, Random random, String label) {
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setUserId(user.getId());
            OrderType orderType = randomOrderType(random);
            order.setOrderType(orderType);
            order.setCreatedAt(LocalDateTime.now().minusDays(i));
            order = orderRepository.save(order);

            int ticketCount = 2 + random.nextInt(3);
            for (int j = 0; j < ticketCount; j++) {
                Ticket ticket = new Ticket();
                ticket.setOrder(order);
                ticket.setShow(shows.get(random.nextInt(shows.size())));
                ticket.setSector(sectors.get(random.nextInt(sectors.size())));
                ticket.setCreatedAt(LocalDateTime.now().minusDays(i));

                TicketStatus status = switch (orderType) {
                    case ORDER -> TicketStatus.BOUGHT;
                    case RESERVATION -> random.nextInt(10) < 7 ? TicketStatus.RESERVED : TicketStatus.EXPIRED;
                    case REFUND -> random.nextInt(10) < 7 ? TicketStatus.REFUNDED : TicketStatus.CANCELLED;
                    default -> TicketStatus.BOUGHT;
                };
                ticket.setStatus(status);
                ticketRepository.save(ticket);
            }
        }
        LOGGER.debug("Created 10 test orders for {}", label);
    }

}
