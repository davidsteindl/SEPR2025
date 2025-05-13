package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;

public class RoomDetailDto {

    @NotNull(message = "ID must not be null")
    @Positive(message = "ID must be positive")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotNull(message = "Sectors must not be null")
    private List<SectorDto> sectors;

    private boolean isHorizontal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SectorDto> getSectors() {
        return sectors;
    }

    public void setSectors(List<SectorDto> sectors) {
        this.sectors = sectors;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public void setHorizontal(boolean isHorizontal) {
        this.isHorizontal = isHorizontal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, sectors, isHorizontal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RoomDetailDto that = (RoomDetailDto) o;
        return isHorizontal == that.isHorizontal
                && Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(sectors, that.sectors);
    }

    @Override
    public String toString() {
        return "RoomDetailDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sectors=" + sectors +
                ", isHorizontal=" + isHorizontal +
                '}';
    }

    public static final class RoomDetailDtoBuilder {
        private Long id;
        private String name;
        private List<SectorDto> sectors;
        private boolean isHorizontal;

        private RoomDetailDtoBuilder() {
        }

        public static RoomDetailDtoBuilder aRoomDetailDto() {
            return new RoomDetailDtoBuilder();
        }

        public RoomDetailDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoomDetailDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomDetailDtoBuilder sectors(List<SectorDto> sectors) {
            this.sectors = sectors;
            return this;
        }

        public RoomDetailDtoBuilder isHorizontal(boolean isHorizontal) {
            this.isHorizontal = isHorizontal;
            return this;
        }

        public RoomDetailDto build() {
            RoomDetailDto dto = new RoomDetailDto();
            dto.setId(id);
            dto.setName(name);
            dto.setSectors(sectors);
            dto.setHorizontal(isHorizontal);
            return dto;
        }
    }
}
