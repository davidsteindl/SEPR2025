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


    @Override
    public int hashCode() {
        return Objects.hash(id, rowNumber, columnNumber, deleted);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SeatDto)) {
            return false;
        }
        SeatDto that = (SeatDto) o;
        return rowNumber == that.rowNumber
            && columnNumber == that.columnNumber
            && deleted == that.deleted
            && Objects.equals(id, that.id);
    }

    @Override
    public String toString() {
        return "SeatDto{"
            + "id=" + id
            + ", rowNumber=" + rowNumber
            + ", columnNumber=" + columnNumber
            + ", deleted=" + deleted
            + '}';
    }

    public static final class SeatDtoBuilder {
        private Long id;
        private int rowNumber;
        private int columnNumber;
        private boolean deleted;

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

        public SeatDto build() {
            SeatDto dto = new SeatDto();
            dto.setId(id);
            dto.setRowNumber(rowNumber);
            dto.setColumnNumber(columnNumber);
            dto.setDeleted(deleted);
            return dto;
        }
    }
}