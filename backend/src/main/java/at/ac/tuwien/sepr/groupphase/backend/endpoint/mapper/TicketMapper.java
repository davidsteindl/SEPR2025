package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "id",           target = "id")
    @Mapping(source = "show.name",    target = "showName")
    @Mapping(source = "show.id", target = "showId")
    @Mapping(source = "sector.price", target = "price")
    @Mapping(source = "sector.id",    target = "sectorId")
    @Mapping(source = "seat.id",      target = "seatId", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "status",       target = "status")
    TicketDto toDto(Ticket ticket);

}