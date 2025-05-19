package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;
import at.ac.tuwien.sepr.groupphase.backend.config.type.PaymentType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDetailDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class OrderDto {
    private Long id;
    private LocalDateTime createdAt;
    private List<TicketDto> tickets;
    private PaymentType paymentType;
    private UserDetailDto customer;
    private OrderType orderType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<TicketDto> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketDto> tickets) {
        this.tickets = tickets;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public UserDetailDto getCustomer() {
        return customer;
    }

    public void setCustomer(UserDetailDto customer) {
        this.customer = customer;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }
}
