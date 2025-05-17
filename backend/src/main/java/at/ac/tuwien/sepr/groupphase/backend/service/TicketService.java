package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;

public interface TicketService {

    public OrderDto getOrderById(Long id);

    public TicketDto getTicketById(Long id);
}
