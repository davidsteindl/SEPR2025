package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = TicketMapper.class)
public abstract class OrderMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "paymentType", target = "paymentType") // wenn vorhanden
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "orderType", target = "orderType")
    @Mapping(source = "tickets", target = "tickets")
    @Mapping(source = "totalPrice", target = "totalPrice") // wenn berechnet
    public abstract OrderDto toDto(Order order);

    public abstract List<OrderDto> toDto(List<Order> orders);

    @AfterMapping
    protected void addShowInfo(Order order, @MappingTarget OrderDto dto) {
        if (order.getTickets() != null && !order.getTickets().isEmpty()) {
            Ticket firstTicket = order.getTickets().get(0);
            if (firstTicket.getShow() != null) {
                dto.setShowName(firstTicket.getShow().getName());
                dto.setShowDate(firstTicket.getShow().getDate());
                dto.setLocationName(firstTicket.getShow().getRoom().getEventLocation().getName());
            }
        }
    }
}
