package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

public class SeatDto {

    @Positive(message = "Seat ID must be positive")
    private Long id;

    @Min(value = 1, message = "Row number must be at least 1")
    @Max(value = 200, message = "Row number must be 200 at most")
    private int rowNumber;

    @Min(value = 1, message = "Column number must be at least 1")
    @Max(value = 100, message = "Column number must be 100 at most")
    private int columnNumber;

    private boolean deleted;

    private Long sectorId;

    @Positive(message = "Room ID must be positive")
    private Long roomId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SeatDto)) {
            return false;
        }
        SeatDto seatDto = (SeatDto) o;
        return rowNumber == seatDto.rowNumber
            && columnNumber == seatDto.columnNumber
            && deleted == seatDto.deleted
            && Objects.equals(id, seatDto.id)
            && Objects.equals(sectorId, seatDto.sectorId)
            && Objects.equals(roomId, seatDto.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rowNumber, columnNumber, deleted, sectorId, roomId);
    }

    @Override
    public String toString() {
        return "SeatDto{"
            + "id=" + id
            + ", rowNumber=" + rowNumber
            + ", columnNumber=" + columnNumber
            + ", deleted=" + deleted
            + ", sectorId=" + sectorId
            + ", roomId=" + roomId
            + '}';
    }

    public static final class SeatDtoBuilder {
        private Long id;
        private int rowNumber;
        private int columnNumber;
        private boolean deleted;
        private Long sectorId;
        private Long roomId;

        public static SeatDtoBuilder aSeatDto() {
            return new SeatDtoBuilder();
        }

        public SeatDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SeatDtoBuilder rowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
            return this;
        }

        public SeatDtoBuilder columnNumber(int columnNumber) {
            this.columnNumber = columnNumber;
            return this;
        }

        public SeatDtoBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public SeatDtoBuilder sectorId(Long sectorId) {
            this.sectorId = sectorId;
            return this;
        }

        public SeatDtoBuilder roomId(Long roomId) {
            this.roomId = roomId;
            return this;
        }

        public SeatDto build() {
            SeatDto dto = new SeatDto();
            dto.setId(id);
            dto.setRowNumber(rowNumber);
            dto.setColumnNumber(columnNumber);
            dto.setDeleted(deleted);
            dto.setSectorId(sectorId);
            dto.setRoomId(roomId);
            return dto;
        }
    }
}
