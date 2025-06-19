package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
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
 *   <li>System assigns {@linkplain #id} and sets ticket status to RESERVED.</li>
 *   <li>Client views {@linkplain #expiresAt} (30 minutes before show start) and shows the reservation number.</li>
 *   <li>Tickets must be picked up or fully purchased before {@linkplain #expiresAt}, or they revert to EXPIRED.</li>
 * </ol>
 *
 * @see TicketService#reserveTickets(TicketRequestDto)
 */
public class ReservationDto {
    private Long id;
    private LocalDateTime createdAt;
    private List<TicketDto> tickets;
    private Long userId;
    private OrderType orderType;
    private LocalDateTime expiresAt;
    private Long groupId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<TicketDto> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketDto> tickets) {
        this.tickets = tickets;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
