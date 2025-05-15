package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonTypeInfo(
    use     = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property= "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SeatedSectorDto.class,   name = "SEATED"),
    @JsonSubTypes.Type(value = StandingSectorDto.class, name = "STANDING")
})
public abstract class SectorDto {

    @NotNull(message = "Sector ID must not be null")
    @Positive(message = "Sector ID must be positive")
    private Long id;

    @NotNull(message = "Sector type must not be null")
    private SectorType type;

    @Positive(message = "Price must be positive")
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