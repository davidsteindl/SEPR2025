package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Order;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = TicketMapper.class)
public interface OrderMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "orderType", target = "orderType")
    @Mapping(source = "tickets", target = "tickets")
    OrderDto toDto(Order order);

    List<OrderDto> toDto(List<Order> orders);

    @AfterMapping
    default void addShowInfo(Order order, @MappingTarget OrderDto dto) {
        if (order.getTickets() != null && !order.getTickets().isEmpty()) {
            var t = order.getTickets().get(0);
            if (t.getShow() != null) {
                dto.setShowName(t.getShow().getName());
                dto.setShowDate(t.getShow().getDate());
                if (t.getShow().getRoom() != null && t.getShow().getRoom().getEventLocation() != null) {
                    dto.setLocationName(t.getShow().getRoom().getEventLocation().getName());
                }
            }
        }
    }
}

