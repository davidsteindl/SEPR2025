package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "id",           target = "id")
    @Mapping(source = "show.name",    target = "showName")
    @Mapping(source = "sector.price", target = "price")
    @Mapping(source = "sector.id",    target = "sectorId")
    @Mapping(source = "seat.id",      target = "seatId", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "status",       target = "status")
    TicketDto toDto(Ticket ticket);

    @AfterMapping
    default void mapSeatDetails(Ticket ticket, @MappingTarget TicketDto dto) {
        Seat seat = ticket.getSeat();
        if (seat != null) {
            dto.setRowNumber(seat.getRowNumber());
            dto.setSeatLabel(convertColumnNumberToLetter(seat.getColumnNumber()));
        }
    }

    default String convertColumnNumberToLetter(int columnNumber) {
        StringBuilder result = new StringBuilder();
        while (columnNumber > 0) {
            columnNumber--;
            result.insert(0, (char) ('A' + (columnNumber % 26)));
            columnNumber /= 26;
        }
        return result.toString();
    }
}