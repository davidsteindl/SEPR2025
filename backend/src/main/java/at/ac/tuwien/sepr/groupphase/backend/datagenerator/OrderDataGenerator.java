package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.OrderGroup;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.repository.SectorRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.OrderGroupRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final OrderGroupRepository orderGroupRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final SectorRepository sectorRepository;


    public OrderDataGenerator(OrderRepository orderRepository,
                              OrderGroupRepository orderGroupRepository,
                              TicketRepository ticketRepository,
                              UserRepository userRepository,
                              ShowRepository showRepository,
                              SectorRepository sectorRepository) {
        this.orderRepository = orderRepository;
        this.orderGroupRepository = orderGroupRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.sectorRepository = sectorRepository;
    }

    @PostConstruct
    public void generateOrders() {
        LOGGER.debug("Generating test data for orders...");

        List<ApplicationUser> users = userRepository.findAll();
        List<Show> allShows = showRepository.findAll();
        List<Sector> sectors = sectorRepository.findAll();


        if (users.isEmpty() || allShows.isEmpty() || sectors.isEmpty()) {
            LOGGER.warn("No shows, users or sectors available, cannot generate tickets");
            return;
        }


        LocalDateTime now = LocalDateTime.now();

        List<Show> pastShows = allShows.stream()
            .filter(sh -> sh.getDate().isBefore(now))
            .toList();
        List<Show> futureShows = allShows.stream()
            .filter(sh -> sh.getDate().isAfter(now))
            .toList();

        if (pastShows.isEmpty()) {
            LOGGER.warn("No Past-Shows available: historical orders cannot be generated.");
            return;
        }
        if (futureShows.isEmpty()) {
            LOGGER.warn("No Future-Shows available: reservation & purchase orders cannot be generated.");
            return;
        }

        Random random = new Random();

        LOGGER.debug("Generating 50 BOUGHT Orders on Past-Shows");
        generateOrderGroups(50, ORDER, BOUGHT, pastShows, users, sectors, random);

        LOGGER.debug("Generating 20 CANCELLED Orders on Past-Shows");
        generateOrderGroups(20, CANCELLATION, CANCELLED, pastShows, users, sectors, random);

        LOGGER.debug("Generating 20 REFUNDED Orders on Past-Shows");
        generateOrderGroups(20, REFUND, REFUNDED, pastShows, users, sectors, random);


        LOGGER.debug("Generating 100 BOUGHT Orders on Future-Shows");
        generateOrderGroups(100, ORDER, BOUGHT, futureShows, users, sectors, random);
        LOGGER.debug("Generating 25 RESERVATION Orders on Future-Shows");
        generateOrderGroups(25, RESERVATION, RESERVED, futureShows, users, sectors, random);


        long alreadyRefunded = ticketRepository.countByStatus(REFUNDED);
        long toRefund = 250L - alreadyRefunded;
        if (toRefund > 0) {
            LOGGER.debug("Creating additional {} REFUND Orders, until 250 REFUNDED Tickets are reached", toRefund);
            generateSingleTicketOrderGroups(toRefund, pastShows, users, sectors, random, REFUND, REFUNDED);
        } else {
            LOGGER.debug(" {} REFUNDED Tickets already exist, no more needed", alreadyRefunded);
        }

        long alreadyCancelled = ticketRepository.countByStatus(CANCELLED);
        long toCancel = 75L - alreadyCancelled;
        if (toCancel > 0) {
            LOGGER.debug("Creating additional {} CANCELLATION Orders, until 75 CANCELLED Tickets are reached", toCancel);
            generateSingleTicketOrderGroups(toCancel, pastShows, users, sectors, random, CANCELLATION, CANCELLED);
        } else {
            LOGGER.debug(" {} CANCELLED Tickets already exist, no more needed", alreadyCancelled);
        }

        LOGGER.debug("Finished generating all test orders.");
    }

    private void generateOrderGroups(int count,
                                    OrderType orderType,
                                    TicketStatus ticketStatus,
                                    List<Show> targetShows,
                                    List<ApplicationUser> users,
                                    List<Sector> sectors,
                                    Random random) {
        LOGGER.debug("Starting generateOrderGroups(): Generating {} order groups of type {} with status {}", count, orderType, ticketStatus);

        for (int i = 0; i < count; i++) {
            ApplicationUser user = users.get(random.nextInt(users.size()));
            Show show = targetShows.get(random.nextInt(targetShows.size()));

            LocalDateTime showDateTime = show.getDate();
            LocalDateTime createdAt = randomPastDateTimeBefore(random, showDateTime, 180);

            OrderGroup group = new OrderGroup();
            group.setUserId(user.getId());
            group = orderGroupRepository.save(group);

            int ticketCount = 1 + random.nextInt(10);
            for (int j = 0; j < ticketCount; j++) {
                Order order = new Order();
                order.setOrderType(orderType);
                order.setCreatedAt(createdAt);
                order.setOrderGroup(group);
                order.setUserId(user.getId());
                order.setTickets(new ArrayList<>());
                order = orderRepository.save(order);

                Ticket ticket = new Ticket();
                ticket.setShow(show);
                ticket.setSector(sectors.get(random.nextInt(sectors.size())));
                ticket.setCreatedAt(createdAt);
                ticket.setStatus(ticketStatus);
                ticket.setOrders(List.of(order));
                ticketRepository.save(ticket);

                order.getTickets().add(ticket);
                orderRepository.save(order);
            }

            LOGGER.trace("Created OrderGroup id={} (user={}), orderType={} with {} tickets at {}",
                group.getId(), user.getId(), orderType, ticketCount, createdAt);
        }
        LOGGER.debug("generateOrderGroups(): Created {} groups of type {}", count, orderType);
    }


    private void generateSingleTicketOrderGroups(long remainingTicketCount,
                                                 List<Show> targetShows,
                                                 List<ApplicationUser> users,
                                                 List<Sector> sectors,
                                                 Random random,
                                                 OrderType orderType,
                                                 TicketStatus ticketStatus) {
        LOGGER.debug("Starting generateSingleTicketOrderGroups(): Generating {} reamining order groups of type {} with status {}",
            remainingTicketCount, orderType, ticketStatus);

        AtomicInteger createdSum = new AtomicInteger(0);

        while (createdSum.get() < remainingTicketCount) {
            ApplicationUser user = users.get(random.nextInt(users.size()));
            Show show = targetShows.get(random.nextInt(targetShows.size()));

            OrderGroup group = new OrderGroup();
            group.setUserId(user.getId());
            group = orderGroupRepository.save(group);

            LocalDateTime createdAt = randomPastDateTimeBefore(random, show.getDate(), 180);

            Order order = new Order();
            order.setOrderType(orderType);
            order.setCreatedAt(createdAt);
            order.setOrderGroup(group);
            order.setUserId(user.getId());
            order = orderRepository.save(order);

            Ticket ticket = new Ticket();
            ticket.setShow(show);
            ticket.setSector(sectors.get(random.nextInt(sectors.size())));
            ticket.setCreatedAt(createdAt);
            ticket.setStatus(ticketStatus);
            ticket.setOrders(List.of(order));
            ticketRepository.save(ticket);

            createdSum.incrementAndGet();
        }
        LOGGER.debug("generateSingleTicketOrderGroups(): Created {} single‐ticket orders with status {}",
            createdSum.get(), ticketStatus);
    }

    private LocalDateTime randomPastDateTimeBefore(Random random, LocalDateTime latestAllowed, int maxBackDays) {
        LOGGER.debug("Starting randomPastDateTimeBefore(): Generating random past date time before {}", latestAllowed);

        long daysBack = 1 + random.nextInt(maxBackDays);
        LocalDateTime earliest = latestAllowed.minusDays(daysBack);

        earliest = earliest.minusHours(random.nextInt(24))
            .minusMinutes(random.nextInt(60))
            .truncatedTo(ChronoUnit.MINUTES);

        long totalMinutes = ChronoUnit.MINUTES.between(earliest, latestAllowed);
        if (totalMinutes <= 1) {
            return latestAllowed.minusMinutes(1);
        }

        long randomOffset = random.nextInt((int) totalMinutes);

        LOGGER.debug("randomPastDateTimeBefore(): Generated a random past date time before {}", earliest);
        return earliest.plusMinutes(randomOffset).truncatedTo(ChronoUnit.MINUTES);
    }
}
