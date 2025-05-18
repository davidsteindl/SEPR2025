package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import at.ac.tuwien.sepr.groupphase.backend.service.TicketService;
import at.ac.tuwien.sepr.groupphase.backend.config.type.TransactionStatus;

/**
 * Data Transfer Object representing callback data from the external payment gateway.
 *
 * <p>
 * After redirecting back to our system, the (mock) payment gateway returns this payload to inform
 * about the final outcome of the payment session. The service uses this information in
 * {@link TicketService#completePurchase(Long, PaymentCallbackDataDto)} to update ticket statuses
 * and generate the final {@link PaymentResultDto}.
 */
public class PaymentCallbackDataDto {
    String sessionId;
    TransactionStatus transactionStatus;
}
