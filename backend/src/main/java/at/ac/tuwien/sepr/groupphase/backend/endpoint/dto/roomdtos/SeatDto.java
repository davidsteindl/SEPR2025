package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Objects;

public class SeatDto {

    @NotNull(message = "Seat ID must not be null")
    @Positive(message = "Seat ID must be positive")
    private Long id;

    @Positive(message = "Row number must be positive")
    private int rowNumber;

    @Positive(message = "Column number must be positive")
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