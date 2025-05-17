package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import at.ac.tuwien.sepr.groupphase.backend.service.TicketService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object representing a ticket reservation for a specific show.
 *
 * <p>
 * A reservation holds one or more tickets in a reserved state and includes
 * an expiration timestamp by which the tickets must be claimed or purchased,
 * otherwise the reservation will expire and the seats will be released.
 *
 * <p>
 * <strong>Reservation Lifecycle:</strong>
 * <ol>
 *   <li>Client calls {@link TicketService#reserveTickets(TicketRequestDto)} to lock seats.</li>
 *   <li>System assigns {@linkplain #reservationId} and sets ticket status to RESERVED.</li>
 *   <li>Client views {@linkplain #expiresAt} (30 minutes before show start) and shows the reservation number.</li>
 *   <li>Tickets must be picked up or fully purchased before {@linkplain #expiresAt}, or they revert to EXPIRED.</li>
 * </ol>
 *
 * @see TicketService#reserveTickets(TicketRequestDto)
 */
public class ReservationDto {
    private Long reservationId;
    private List<TicketDto> tickets;
    private LocalDateTime expiresAt;
}
