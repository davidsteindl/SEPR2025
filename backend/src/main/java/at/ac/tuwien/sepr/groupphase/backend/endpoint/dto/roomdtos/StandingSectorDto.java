package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@JsonTypeName("STANDING")
public class StandingSectorDto extends SectorDto {

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 9999, message = "Capacity must be less than 10000")
    private int capacity;

    public StandingSectorDto() {
        super();
        setType(SectorType.STANDING);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
