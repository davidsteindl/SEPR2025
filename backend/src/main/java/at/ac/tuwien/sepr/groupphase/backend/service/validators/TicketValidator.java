package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public void validateForReserveTickets(TicketRequestDto ticketRequestDto) {

        LOGGER.debug("validateForReserveTickets: {}", ticketRequestDto);

        // is there a hold on the selected seats?

        // does the show exist?

        // can i reserve tickets for this show? (30 mins before start)

        // is there a ticket booked for this seat at this show already?
    }

    public void validateForBuyReservedTickets(List<Long> ticketIds, List<Ticket> tickets) {
        LOGGER.debug("validateForBuyReservedTickets: ticketIds={}, tickets={}", ticketIds, tickets);

        // is there a hold on the selected seats?

        // are the tickets all status reserved

        // is the resevation still valid? (show start point - 30 mins)

        // am i the user who reserved the tickets?




    }
}
