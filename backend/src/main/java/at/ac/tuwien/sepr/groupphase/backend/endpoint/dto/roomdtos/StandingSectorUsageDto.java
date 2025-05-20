package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

public class StandingSectorUsageDto extends StandingSectorDto {
    private int availableCapacity;

    public int getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(int availableCapacity) {
        this.availableCapacity = availableCapacity;
    }
}
