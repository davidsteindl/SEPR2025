package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShowService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationCleanUpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationCleanUpService.class);

    private final TicketRepository ticketRepository;
    private final ShowService showService;

    public ReservationCleanUpService(TicketRepository ticketRepository, ShowService showService) {
        this.ticketRepository = ticketRepository;
        this.showService = showService;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void cancelExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxShowTime = now.plusMinutes(30);

        LOGGER.info("Running reservation cleanup at {}", now);

        List<Show> upcomingShows = showService.findShowsBetween(now, maxShowTime);

        for (Show show : upcomingShows) {
            List<Ticket> reservedTickets = ticketRepository.findByShowAndStatus(show, TicketStatus.RESERVED);

            for (Ticket ticket : reservedTickets) {
                ticket.setStatus(TicketStatus.CANCELLED);
                LOGGER.info("Auto-cancelled ticket ID={} for show '{}' starting at {}",
                    ticket.getId(), show.getName(), show.getDate());
            }

            ticketRepository.saveAll(reservedTickets);
        }
    }
}
