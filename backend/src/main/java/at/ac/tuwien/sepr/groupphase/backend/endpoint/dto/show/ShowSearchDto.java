package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowSearchDto {

    @NotNull(message = "Page index must not be null")
    @Min(value = 0, message = "Page index must be non-negative")
    private Integer page = 0;

    @NotNull(message = "Page size must not be null")
    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size = 10;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Long eventId;
    private Long roomId;

    private String name;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    @Override
    public String toString() {
        return "ShowSearchDto{"
            + "page=" + page
            + ", size=" + size
            + ", startDate=" + startDate
            + ", endDate=" + endDate
            + ", eventId=" + eventId
            + ", roomId=" + roomId
            + ", name='" + name + '\''
            + ", minPrice=" + minPrice
            + ", maxPrice=" + maxPrice
            + '}';
    }
}
