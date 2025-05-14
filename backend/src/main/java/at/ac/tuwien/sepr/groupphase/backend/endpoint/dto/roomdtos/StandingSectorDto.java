package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

public class StandingSectorDto extends SectorDto {

    @Positive(message = "Capacity must be positive")
    private int capacity;


    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), getPrice(), capacity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StandingSectorDto)) {
            return false;
        }
        StandingSectorDto that = (StandingSectorDto) o;
        return getPrice() == that.getPrice()
               && capacity == that.capacity
               && Objects.equals(getId(), that.getId())
               && getType() == that.getType();
    }

    @Override
    public String toString() {
        return "StandingSectorDto{"
               + "id=" + getId()
               + ", type=" + getType()
               + ", price=" + getPrice()
               + ", capacity=" + capacity
               + '}';
    }


    public static final class StandingSectorDtoBuilder {
        private Long id;
        private int price;
        private int capacity;

        private StandingSectorDtoBuilder() { }

        public static StandingSectorDtoBuilder aStandingSectorDto() {
            return new StandingSectorDtoBuilder();
        }

        public StandingSectorDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public StandingSectorDtoBuilder price(int price) {
            this.price = price;
            return this;
        }

        public StandingSectorDtoBuilder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public StandingSectorDto build() {
            StandingSectorDto dto = new StandingSectorDto();
            dto.setId(id);
            dto.setType(SectorType.STANDING);
            dto.setPrice(price);
            dto.setCapacity(capacity);
            return dto;
        }
    }
}
