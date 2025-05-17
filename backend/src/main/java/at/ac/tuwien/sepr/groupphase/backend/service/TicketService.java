package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;

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
}
