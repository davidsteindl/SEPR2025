package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TicketValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketValidator.class);

    public void validateForBuyTickets(TicketRequestDto ticketRequestDto) {
        LOGGER.debug("validateForBuyTickets: {}", ticketRequestDto);
    }
}
