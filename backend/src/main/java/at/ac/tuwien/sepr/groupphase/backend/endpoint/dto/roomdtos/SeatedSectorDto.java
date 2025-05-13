package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Objects;

public class SeatedSectorDto extends SectorDto {

    @NotEmpty(message = "Rows must not be empty")
    private List<SeatDto> rows;

    public List<SeatDto> getRows() {
        return rows;
    }

    public void setRows(List<SeatDto> rows) {
        this.rows = rows;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), getPrice(), rows);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SeatedSectorDto))
            return false;
        SeatedSectorDto that = (SeatedSectorDto) o;
        return Objects.equals(getId(), that.getId()) &&
                getType() == that.getType() &&
                getPrice() == that.getPrice() &&
                Objects.equals(rows, that.rows);
    }

    @Override
    public String toString() {
        return "SeatedSectorDto{" +
                "id=" + getId() +
                ", type=" + getType() +
                ", price=" + getPrice() +
                ", rows=" + rows +
                '}';
    }

    public static final class SeatedSectorDtoBuilder {
        private Long id;
        private int price;
        private List<SeatDto> rows;

        private SeatedSectorDtoBuilder() {
        }

        public static SeatedSectorDtoBuilder aSeatedSectorDto() {
            return new SeatedSectorDtoBuilder();
        }

        public SeatedSectorDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SeatedSectorDtoBuilder price(int price) {
            this.price = price;
            return this;
        }

        public SeatedSectorDtoBuilder rows(List<SeatDto> rows) {
            this.rows = rows;
            return this;
        }

        public SeatedSectorDto build() {
            SeatedSectorDto dto = new SeatedSectorDto();
            dto.setId(id);
            dto.setType(SectorType.SEATED);
            dto.setPrice(price);
            dto.setRows(rows);
            return dto;
        }
    }
}
