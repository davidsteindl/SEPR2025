package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;


import at.ac.tuwien.sepr.groupphase.backend.service.TicketService;

/**
 * Data Transfer Object representing the initiated payment session for a ticket purchase or reservation.
 *
 * <p>
 * After selecting seats (seated or standing) via the graphical seat map, the system uses this DTO
 * to return the payment gateway session information. Tickets are placed into {@code PAYMENT_PENDING}
 * state until the payment process is completed or expires.
 *
 * <p><strong>Usage Scenarios:</strong>
 * <ul>
 *   <li><strong>Immediate Purchase:</strong> Returned by {@code buyTickets} when the customer opts to pay immediately.</li>
 *   <li><strong>Purchasing Reserved Tickets:</strong> Returned by {@code buyReservedTickets} when converting reservations into purchases.</li>
 * </ul>
 *
 * <p>
 * The client must redirect the user to {@code paymentUrl} or use the provided session ID
 * to complete the transaction in the (mock) external payment gateway. Upon callback, {@link
 * TicketService#completePurchase(Long, PaymentCallbackDataDto)} uses this session ID to finalize the purchase.
 *
 * @see TicketService#buyTickets(TicketRequestDto)
 * @see TicketService#buyReservedTickets(Long, java.util.List)
 */
public class PaymentSessionDto {
    private Long sessionId;
    private String paymentUrl;
    private TicketRequestDto target;
    private int totalPrice;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public TicketRequestDto getTarget() {
        return target;
    }

    public void setTarget(TicketRequestDto target) {
        this.target = target;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}
