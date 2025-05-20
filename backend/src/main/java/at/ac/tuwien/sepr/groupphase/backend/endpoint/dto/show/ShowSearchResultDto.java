package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowSearchResultDto {

    private Long id;
    private String name;
    private int duration;
    private LocalDateTime date;

    private Long eventId;
    private String eventName;

    private Long roomId;
    private String roomName;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
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
        return "ShowSearchResultDto{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", duration=" + duration
            + ", date=" + date
            + ", eventId=" + eventId
            + ", eventName='" + eventName + '\''
            + ", roomId=" + roomId
            + ", roomName='" + roomName + '\''
            + ", minPrice=" + minPrice
            + ", maxPrice=" + maxPrice
            + '}';
    }
}
