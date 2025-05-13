package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

public class SeatDto {

    @NotNull(message = "Seat ID must not be null")
    @Positive(message = "Seat ID must be positive")
    private Long id;

    @Positive(message = "Seat number must be positive")
    private int seatNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, seatNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SeatDto))
            return false;
        SeatDto that = (SeatDto) o;
        return seatNumber == that.seatNumber &&
                Objects.equals(id, that.id);
    }

    @Override
    public String toString() {
        return "SeatDto{" +
                "id=" + id +
                ", seatNumber=" + seatNumber +
                '}';
    }

    public static final class SeatDtoBuilder {
        private Long id;
        private int seatNumber;

        private SeatDtoBuilder() {
        }

        public static SeatDtoBuilder aSeatDto() {
            return new SeatDtoBuilder();
        }

        public SeatDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SeatDtoBuilder seatNumber(int seatNumber) {
            this.seatNumber = seatNumber;
            return this;
        }

        public SeatDto build() {
            SeatDto dto = new SeatDto();
            dto.setId(id);
            dto.setSeatNumber(seatNumber);
            return dto;
        }
    }
}
