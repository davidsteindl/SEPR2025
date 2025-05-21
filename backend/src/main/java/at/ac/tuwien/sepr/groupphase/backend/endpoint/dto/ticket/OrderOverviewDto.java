package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import at.ac.tuwien.sepr.groupphase.backend.config.type.OrderType;

import java.time.LocalDateTime;
import java.util.Objects;

public class OrderOverviewDto {

    private Long orderId;
    private LocalDateTime createdAt;
    private String showName;
    private LocalDateTime showDate;
    private String locationName;
    private OrderType orderType;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public LocalDateTime getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDateTime showDate) {
        this.showDate = showDate;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderOverviewDto that)) {
            return false;
        }
        return Objects.equals(orderId, that.orderId)
            && Objects.equals(createdAt, that.createdAt)
            && Objects.equals(showName, that.showName)
            && Objects.equals(showDate, that.showDate)
            && Objects.equals(locationName, that.locationName)
            && orderType == that.orderType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, createdAt, showName, showDate, locationName, orderType);
    }

    @Override
    public String toString() {
        return "OrderOverviewDto{"
            + "orderId=" + orderId
            + ", createdAt=" + createdAt
            + ", showName='" + showName + '\''
            + ", showDate=" + showDate
            + ", locationName='" + locationName + '\''
            + ", orderType=" + orderType
            + '}';
    }

    public static final class OrderOverviewDtoBuilder {
        private Long orderId;
        private LocalDateTime createdAt;
        private String showName;
        private LocalDateTime showDate;
        private String locationName;
        private OrderType orderType;

        private OrderOverviewDtoBuilder() {}

        public static OrderOverviewDtoBuilder anOrderOverviewDto() {
            return new OrderOverviewDtoBuilder();
        }

        public OrderOverviewDtoBuilder withOrderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderOverviewDtoBuilder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OrderOverviewDtoBuilder withShowName(String showName) {
            this.showName = showName;
            return this;
        }

        public OrderOverviewDtoBuilder withShowDate(LocalDateTime showDate) {
            this.showDate = showDate;
            return this;
        }

        public OrderOverviewDtoBuilder withLocationName(String locationName) {
            this.locationName = locationName;
            return this;
        }

        public OrderOverviewDtoBuilder withOrderType(OrderType orderType) {
            this.orderType = orderType;
            return this;
        }

        public OrderOverviewDto build() {
            OrderOverviewDto dto = new OrderOverviewDto();
            dto.setOrderId(orderId);
            dto.setCreatedAt(createdAt);
            dto.setShowName(showName);
            dto.setShowDate(showDate);
            dto.setLocationName(locationName);
            dto.setOrderType(orderType);
            return dto;
        }
    }
}
