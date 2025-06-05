package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.Positive;

@JsonTypeName("STANDING")
public class StandingSectorDto extends SectorDto {

    @Positive(message = "Capacity must be a positive number or zero")
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
