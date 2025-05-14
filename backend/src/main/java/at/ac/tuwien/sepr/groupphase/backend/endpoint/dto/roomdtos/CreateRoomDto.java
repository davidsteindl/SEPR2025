package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateRoomDto {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @Positive(message = "Initial number of sectors must be positive")
    private int numberOfSectors;

    @Positive(message = "Initial number of rows per sector must be positive")
    private int rowsPerSector;

    @Positive(message = "Initial number of seats per row must be positive")
    private int seatsPerRow;

    @NotNull(message = "Event location ID must not be null")
    private Long eventLocationId;

    private boolean isHorizontal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfSectors() {
        return numberOfSectors;
    }

    public void setNumberOfSectors(int numberOfSectors) {
        this.numberOfSectors = numberOfSectors;
    }

    public int getRowsPerSector() {
        return rowsPerSector;
    }

    public void setRowsPerSector(int rowsPerSector) {
        this.rowsPerSector = rowsPerSector;
    }

    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(int seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }

    public Long getEventLocationId() {
        return eventLocationId;
    }

    public void setEventLocationId(Long eventLocationId) {
        this.eventLocationId = eventLocationId;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public void setHorizontal(boolean isHorizontal) {
        this.isHorizontal = isHorizontal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, numberOfSectors, rowsPerSector, seatsPerRow, eventLocationId, isHorizontal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateRoomDto that = (CreateRoomDto) o;
        return numberOfSectors == that.numberOfSectors
                && rowsPerSector == that.rowsPerSector
                && seatsPerRow == that.seatsPerRow
                && isHorizontal == that.isHorizontal
                && Objects.equals(name, that.name)
                && Objects.equals(eventLocationId, that.eventLocationId);
    }

    @Override
    public String toString() {
        return "CreateRoomDto{"
                + "name='" + name + '\''
                + ", numberOfSectors=" + numberOfSectors
                + ", rowsPerSector=" + rowsPerSector
                + ", seatsPerRow=" + seatsPerRow
                + ", eventLocationId=" + eventLocationId
                + ", isHorizontal=" + isHorizontal
                + '}';
    }

    public static final class CreateRoomDtoBuilder {
        private String name;
        private int numberOfSectors;
        private int rowsPerSector;
        private int seatsPerRow;
        private Long eventLocationId;
        private boolean isHorizontal;

        private CreateRoomDtoBuilder() {
        }

        public static CreateRoomDtoBuilder aCreateRoomDtoBuilder() {
            return new CreateRoomDtoBuilder();
        }

        public CreateRoomDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CreateRoomDtoBuilder numberOfSectors(int numberOfSectors) {
            this.numberOfSectors = numberOfSectors;
            return this;
        }

        public CreateRoomDtoBuilder rowsPerSector(int rowsPerSector) {
            this.rowsPerSector = rowsPerSector;
            return this;
        }

        public CreateRoomDtoBuilder seatsPerRow(int seatsPerRow) {
            this.seatsPerRow = seatsPerRow;
            return this;
        }

        public CreateRoomDtoBuilder eventLocationId(Long eventLocationId) {
            this.eventLocationId = eventLocationId;
            return this;
        }

        public CreateRoomDtoBuilder isHorizontal(boolean isHorizontal) {
            this.isHorizontal = isHorizontal;
            return this;
        }

        public CreateRoomDto build() {
            CreateRoomDto dto = new CreateRoomDto();
            dto.setName(name);
            dto.setNumberOfSectors(numberOfSectors);
            dto.setRowsPerSector(rowsPerSector);
            dto.setSeatsPerRow(seatsPerRow);
            dto.setEventLocationId(eventLocationId);
            dto.setHorizontal(isHorizontal);
            return dto;
        }
    }
}
