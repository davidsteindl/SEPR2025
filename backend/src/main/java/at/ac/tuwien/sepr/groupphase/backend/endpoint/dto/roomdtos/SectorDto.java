package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = StandingSectorDto.class, name = "STANDING"),
    @JsonSubTypes.Type(value = StageSectorDto.class, name = "STAGE")
})
public class SectorDto {

    @Positive(message = "Sector ID must be positive")
    private Long id;

    @NotNull(message = "Sector type must not be null")
    private SectorType type;

    @Min(value = 1, message = "Price must be at least 1")
    @Max(value = 9999, message = "Price must be less than 10000")
    private int price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SectorType getType() {
        return type;
    }

    public void setType(SectorType type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
