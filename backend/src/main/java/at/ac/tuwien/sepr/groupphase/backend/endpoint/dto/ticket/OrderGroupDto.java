package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import java.time.LocalDateTime;
import java.util.List;

public class OrderGroupDto {
    private Long id;

    private String showName;
    private LocalDateTime showDate;
    private String locationName;

    private List<OrderDto> orders;


    public OrderGroupDto() {
    }

    public OrderGroupDto(Long id, String showName, LocalDateTime showDate, String locationName, int totalPrice, List<OrderDto> orders) {
        this.id = id;
        this.showName = showName;
        this.showDate = showDate;
        this.locationName = locationName;
        this.orders = orders;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<OrderDto> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderDto> orders) {
        this.orders = orders;
    }

}
