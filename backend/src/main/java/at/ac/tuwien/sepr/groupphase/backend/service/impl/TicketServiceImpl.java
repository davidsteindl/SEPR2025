package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.OrderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.PaymentCallbackDataDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.PaymentResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.PaymentSessionDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.ReservationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.service.TicketService;

import java.util.List;

public class TicketServiceImpl implements TicketService {

    @Override
    public OrderDto getOrderById(Long id) {
        return null;
    }

    @Override
    public TicketDto getTicketById(Long id) {
        return null;
    }

    @Override
    public PaymentSessionDto buyTickets(TicketRequestDto ticketRequestDto) {
        return null;
    }

    @Override
    public ReservationDto reserveTickets(TicketRequestDto ticketRequestDto) {
        return null;
    }

    @Override
    public PaymentSessionDto buyReservedTickets(Long reservationId, List<Long> ticketIds) {
        return null;
    }

    @Override
    public List<TicketDto> cancelReservations(List<Long> ticketIds) {
        return List.of();
    }

    @Override
    public List<TicketDto> refundTickets(List<Long> ticketIds) {
        return List.of();
    }

    @Override
    public PaymentResultDto completePurchase(Long sessionId, PaymentCallbackDataDto paymentCallbackDataDto) {
        return null;
    }
}
