package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;

/**
 * Data Transfer Object representing an individual ticket for a show.
 *
 * <p>
 * Contains all information necessary to display and manage a ticket,
 * including show name, price, seating or standing target details, and current status.
 * This DTO supports various ticketing operations such as display in user overviews,
 * seat-map highlighting, purchases, reservations, and cancellations.
 *
 * <p><strong>Lifecycle States:</strong>
 * <ul>
 *   <li>{@link TicketStatus#RESERVED RESERVED}: Customer has confirmed a hold for later payment.</li>
 *   <li>{@link TicketStatus#PAYMENT_PENDING PAYMENT_PENDING}: Ticket is in the payment gateway flow.</li>
 *   <li>{@link TicketStatus#BOUGHT BOUGHT}: Purchase completed successfully.</li>
 *   <li>{@link TicketStatus#REFUNDED REFUNDED}: Ticket was refunded and seat freed.</li>
 *   <li>{@link TicketStatus#EXPIRED EXPIRED}: Hold or reservation expired without completion.</li>
 *   <li>{@link TicketStatus#CANCELLED CANCELLED}: User-initiated cancellation of a reservation or purchase.</li>
 * </ul>
 *
 * @see TicketStatus
 * @see TicketTargetDto
 */
public class TicketDto {
    private Long id;
    private String showName;
    private int price;
    private TicketTargetDto target;
    private TicketStatus status;
}
