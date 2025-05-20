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

        // is there a hold on the selected seats?

        // does the show exist?

        // can i buy tickets for this show? (30 mins before start)

        // is there a ticket booked for this seat at this show already?
    }

    public void validateHold(Long showId, Long sectorId, Long seatId, Long userId) {

        LOGGER.debug("validateHold: showId={}, sectorId={}, seatId={}, userId={}", showId, sectorId, seatId, userId);

        // is there a currently valid hold on the selected seat?

        // is there a ticket booked for this seat at this show?

        // does the show exist?

        // can i hold tickets for this show?

        // is the seat in the selected sector?

        // is the sector part of the show?

        // is the seat part of the sector? (if it is a seated sector)


    }
}
