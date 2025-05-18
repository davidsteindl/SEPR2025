package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import at.ac.tuwien.sepr.groupphase.backend.service.TicketService;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TransactionStatus;

import java.util.List;

/**
 * Data Transfer Object representing the outcome of a payment session for ticket purchases.
 *
 * <p>
 * After redirecting to and returning from the external payment gateway, this DTO captures
 * the final transaction status along with details of tickets successfully purchased,
 * total amount charged, and currency. This information is used to provide feedback to the client.
 *
 * @see TicketService#completePurchase(Long, PaymentCallbackDataDto)
 * @see TransactionStatus
 */
public class PaymentResultDto {
    private List<TicketDto> purchasedTickets;
    private long amount;
    private TransactionStatus transactionStatus;
}
