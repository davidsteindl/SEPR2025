package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import at.ac.tuwien.sepr.groupphase.backend.config.type.PaymentType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDetailDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class OrderDto {
    private Long id;
    private Long userId;
    private LocalDateTime date;
    private List<TicketDto> tickets;
    private PaymentType paymentType;
    private UserDetailDto customer;
    private PaymentAdressDto paymentAdress;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
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

    public PaymentAdressDto getPaymentAdress() {
        return paymentAdress;
    }

    public void setPaymentAdress(PaymentAdressDto paymentAdress) {
        this.paymentAdress = paymentAdress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderDto that)) {
            return false;
        }
        return Objects.equals(id, that.id)
            && Objects.equals(userId, that.userId)
            && Objects.equals(date, that.date)
            && Objects.equals(tickets, that.tickets)
            && paymentType == that.paymentType
            && Objects.equals(customer, that.customer)
            && Objects.equals(paymentAdress, that.paymentAdress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, date, tickets, paymentType, customer, paymentAdress);
    }

    @Override
    public String toString() {
        return "OrderDto{" +
            "id=" + id +
            ", userId=" + userId +
            ", date=" + date +
            ", tickets=" + tickets +
            ", paymentType=" + paymentType +
            ", customer=" + customer +
            ", paymentAdress=" + paymentAdress +
            '}';
    }

    public static final class OrderDtoBuilder {
        private Long id;
        private Long userId;
        private LocalDateTime date;
        private List<TicketDto> tickets;
        private PaymentType paymentType;
        private UserDetailDto customer;
        private PaymentAdressDto paymentAdress;

        private OrderDtoBuilder() {
        }

        public static OrderDtoBuilder anOrderDto() {
            return new OrderDtoBuilder();
        }

        public OrderDtoBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public OrderDtoBuilder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public OrderDtoBuilder withDate(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public OrderDtoBuilder withTickets(List<TicketDto> tickets) {
            this.tickets = tickets;
            return this;
        }

        public OrderDtoBuilder withPaymentType(PaymentType paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public OrderDtoBuilder withCustomer(UserDetailDto customer) {
            this.customer = customer;
            return this;
        }

        public OrderDtoBuilder withPaymentAdress(PaymentAdressDto paymentAdress) {
            this.paymentAdress = paymentAdress;
            return this;
        }

        public OrderDto build() {
            OrderDto dto = new OrderDto();
            dto.setId(id);
            dto.setUserId(userId);
            dto.setDate(date);
            dto.setTickets(tickets);
            dto.setPaymentType(paymentType);
            dto.setCustomer(customer);
            dto.setPaymentAdress(paymentAdress);
            return dto;
        }
    }
}
