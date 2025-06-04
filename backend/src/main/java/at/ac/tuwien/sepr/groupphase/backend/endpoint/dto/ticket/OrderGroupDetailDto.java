package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import java.util.List;

public class OrderGroupDetailDto extends OrderGroupDto {
    private List<TicketDto> tickets;

    public List<TicketDto> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketDto> tickets) {
        this.tickets = tickets;
    }
}
