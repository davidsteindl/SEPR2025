package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

/**
 * DTO for creating a hold.
 */
public class CreateHoldDto {
    private Long showId;
    private Long sectorId;
    private Long seatId;
    private Long userId;

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}