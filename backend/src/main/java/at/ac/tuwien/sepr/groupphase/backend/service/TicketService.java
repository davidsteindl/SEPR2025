package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.PaymentCallbackDataDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.PaymentResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.PaymentSessionDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.ReservationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetDto;

import java.util.List;


/**
 * Service for ticket-related operations, including searching, purchasing, reserving,
 * and managing ticket life-cycle events such as cancellations and refunds.
 *
 */
public interface TicketService {

    /**
     * Fetches the order identified by the given ID.
     *
     * @param id the unique identifier of the order to retrieve
     * @return the {@link OrderDto} representing that order, or null if no order with the given ID exists
     */
    public OrderDto getOrderById(Long id);

    /**
     * Fetches the ticket identified by the given ID.
     *
     * @param id the unique identifier of the ticket to retrieve
     * @return the {@link TicketDto} representing that ticket, or null if no ticket with the given ID exists
     */
    public TicketDto getTicketById(Long id);

    /**
     * Initiates the purchase of tickets for a show.
     *
     * <p>
     * Targets are selected via a graphical seat map and include specific seats (row + seat)
     * or standing-sector quantities. Tickets are placed into {@code PAYMENT_PENDING} status until
     * the payment is completed. Returns a payment session containing the URL or token for redirection.
     *
     * @param ticketRequestDto the request payload including show ID and list of {@link TicketTargetDto targets} to purchase;
     *                         must include at least one target
     * @return a {@link PaymentSessionDto} containing the payment gateway session ID and redirect URL
     */
    PaymentSessionDto buyTickets(TicketRequestDto ticketRequestDto);

    /**
     * Reserves (holds) tickets for later purchase.
     *
     * <p>
     * Selected seats are placed into {@code RESERVED} state, until purchased either online or
     * in-person at the venue. The response includes a reservation identifier and expiry timestamp, which the UI must display
     * with a notice to pick up tickets at least 30 minutes before the event or the reservation expires.
     *
     * @param ticketRequestDto the request payload including show ID and list of targets to reserve;
     *                         must include at least one target
     * @return a {@link ReservationDto} containing the reservation ID, reserved tickets, and expiry time
     */
    ReservationDto reserveTickets(TicketRequestDto ticketRequestDto);

    /**
     * Initiates purchase for tickets that were previously reserved under a reservation.
     *
     * <p>
     * Allows partial conversion of a reservation by specifying a subset of ticket IDs. Tickets
     * not included in this purchase remain in {@code RESERVED} state or will expire per original terms.
     * Returned tickets enter {@code PAYMENT_PENDING} status until {@link #completePurchase} is called.
     *
     * @param reservationId the identifier of the existing reservation; must not be {@code null}
     * @param ticketIds the list of ticket IDs to purchase; must be a non-empty subset of tickets from the reservation
     * @return a {@link PaymentSessionDto} for completing the payment
     */
    PaymentSessionDto buyReservedTickets(Long reservationId, List<Long> ticketIds);

    /**
     * Cancels existing ticket reservations.
     *
     * <p>
     * Cancelling a reservation frees the held seats, returning them to available status for other customers.
     * Tickets returned will carry {@code CANCELLED} or {@code EXPIRED} status. Partial or full cancellations
     * of a reservation are supported.
     *
     * @param ticketIds the list of reserved ticket IDs to cancel; must not be {@code null} or empty
     * @return updated {@link TicketDto} objects reflecting new status for each cancelled ticket
     */
    List<TicketDto> cancelReservations(List<Long> ticketIds);

    /**
     * Processes refunds for purchased tickets.
     *
     * <p>
     * Refunds convert tickets from {@code BOUGHT} to {@code REFUNDED} status and make those seats
     * available again. Supports batch refunding of multiple tickets in one call.
     *
     * @param ticketIds the list of purchased ticket IDs to refund; must not be {@code null} or empty
     * @return updated {@link TicketDto} objects reflecting refunded status
     */
    List<TicketDto> refundTickets(List<Long> ticketIds);


    /**
     * Completes a pending payment after redirection from the payment gateway.
     *
     * <p>
     * This callback verifies the payment result for the given session, updates ticket statuses from
     * {@code PAYMENT_PENDING} to {@code BOUGHT} on success (or handles failures accordingly), and
     * generates a {@link PaymentResultDto}.
     *
     * @param sessionId the (mock) payment session ID associated with the purchase; must not be {@code null}
     * @param paymentCallbackDataDto data returned by the payment gateway including final transaction status
     * @return a {@link PaymentResultDto} containing purchased tickets, total amount, currency, and transaction status
     */
    PaymentResultDto completePurchase(Long sessionId, PaymentCallbackDataDto paymentCallbackDataDto);


}
