package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Objects;

public class SeatUsageDto extends SeatDto {
    private boolean available;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}