package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.CreateHoldDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.ReservationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ReservationExpiredException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ReservationNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.SeatUnavailableException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * Purchases one or more tickets immediately for a given event/sector/seat‐selection.
     *
     * <p>customer chooses seats via the
     * graphical hall‐plan and may select multiple seats.
     * In the backend, all requested seats must be still available; otherwise an exception is thrown.
     * On success, a new Order is created with status BOUGHT for each Ticket.</p>
     *
     * @param ticketRequestDto DTO containing event ID, sector info, seat‐selection (row, seat), and desired quantities.
     * @return an {@link OrderDto} summarizing the purchase: order ID, list of tickets (with status BOUGHT), total price.
     * @throws IllegalArgumentException if the request is malformed (e.g. invalid event or sector ID).
     * @throws SeatUnavailableException if one or more requested seats have already been taken.
     */
    OrderDto buyTickets(TicketRequestDto ticketRequestDto);


    /**
     * Reserves one or more tickets for later purchase.
     *
     * <p>Customer selects seats
     * on the graphical hall‐plan (multi‐select).  The reservation holds until 30 minutes
     * before the beginning of the show, then it automatically expires (status EXPIRED).
     * On success, returns a reservation DTO containing the reservation ID and expiry timestamp.</p>
     *
     * @param ticketRequestDto DTO containing event ID, sector info, seat‐selection and quantities.
     * @return a {@link ReservationDto} with reservation ID, expiry time, and reserved tickets (status RESERVED).
     * @throws IllegalArgumentException if the request is malformed.
     * @throws SeatUnavailableException if any seat is no longer free.
     */
    ReservationDto reserveTickets(TicketRequestDto ticketRequestDto);


    /**
     * Converts an existing reservation into a purchase.
     *
     * <p>customer may buy all or just a subset of previously reserved tickets.
     * Any tickets that are not purchased will remain in the reserved order object.
     * Successfully purchased tickets transition to BOUGHT. A new order object
     * will be created for the purchased tickets.</p>
     *
     * @param ticketIds the subset of ticket‐IDs within the reservation to purchase.
     * @return an {@link OrderDto} representing the new Order of purchased tickets.
     * @throws ReservationNotFoundException if the reservationId does not exist.
     * @throws ReservationExpiredException if the reservation has already expired.
     * @throws IllegalArgumentException if any ticketId is not part of the given reservation.
     */
    OrderDto buyReservedTickets(List<Long> ticketIds);



    /**
     * Cancels one or more reservations before they are paid.
     *
     * <p>customer may cancel reserved tickets; those seats are released (status CANCELLED).</p>
     *
     * @param ticketIds list of reserved ticket IDs to cancel.
     * @return a list of {@link TicketDto} for the cancelled tickets (status CANCELLED).
     * @throws IllegalArgumentException if any ticketId is invalid or not in RESERVED status.
     */
    List<TicketDto> cancelReservations(List<Long> ticketIds);


    /**
     * Refunds one or more already‐bought tickets.
     *
     * <p>customer may refund purchased tickets; refunded tickets transition to REFUNDED
     * and the associated seats are released.</p>
     *
     * @param ticketIds list of ticket IDs currently in BOUGHT status to refund.
     * @return a list of {@link TicketDto} for the refunded tickets (status REFUNDED).
     * @throws IllegalArgumentException if any ticketId is invalid or not in BOUGHT status.
     */
    List<TicketDto> refundTickets(List<Long> ticketIds);

    /**
     * Creates a temporary hold for a seat in a show.
     * Once the hold is created, the seat is reserved for 30 minutes.
     * In this time, no other user can book the seat. If the user does not
     * book the seat within 30 minutes, the hold is released.
     *
     * @param createHoldDto DTO containing event ID, sector info, seat‐selection, ...
     */
    void createTicketHold(CreateHoldDto createHoldDto);


    /**
     * Retrieves a paginated list of orders (purchases or reservations) for the given user.
     *
     * @param userId the ID of the user whose orders should be fetched
     * @param type the type of the order: ODER or RESERVATION
     * @param past if true, only orders for past shows are returned; if false, only future shows
     * @param pageable pagination information (page number, size, sort)
     * @return a paginated list of {@link OrderDto} objects
     */
    Page<OrderDto> getOrdersForUser(Long userId, OrderType type, boolean past, Pageable pageable);
}
