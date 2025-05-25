package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

public class SeatUsageDto extends SeatDto {
    private boolean available;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}