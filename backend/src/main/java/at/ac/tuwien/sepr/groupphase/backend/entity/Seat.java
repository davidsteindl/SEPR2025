package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

@Entity
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive(message = "Row number must be positive")
    private int rowNumber;

    @Positive(message = "Column number must be positive")
    private int columnNumber;

    @Column(nullable = false)
    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

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

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Seat seat)) {
            return false;
        }
        return rowNumber == seat.rowNumber
            && columnNumber == seat.columnNumber
            && deleted == seat.deleted
            && Objects.equals(id, seat.id)
            && Objects.equals(sector, seat.sector)
            && Objects.equals(room, seat.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rowNumber, columnNumber, deleted, sector, room);
    }

    @Override
    public String toString() {
        return "Seat{"
            + "id=" + id
            + ", row=" + rowNumber
            + ", column=" + columnNumber
            + ", deleted=" + deleted
            + ", sector ID=" + (sector != null ? sector.getId() : "null")
            + ", room ID=" + (room != null ? room.getId() : "null")
            + '}';
    }

    public static final class SeatBuilder {
        private int rowNumber;
        private int columnNumber;
        private boolean deleted;
        private Sector sector;
        private Room room;

        private SeatBuilder() {
        }

        public static SeatBuilder aSeat() {
            return new SeatBuilder();
        }

        public SeatBuilder withRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
            return this;
        }

        public SeatBuilder withColumnNumber(int columnNumber) {
            this.columnNumber = columnNumber;
            return this;
        }

        public SeatBuilder withDeleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public SeatBuilder withSector(Sector sector) {
            this.sector = sector;
            return this;
        }

        public SeatBuilder withRoom(Room room) {
            this.room = room;
            return this;
        }

        public Seat build() {
            Seat seat = new Seat();
            seat.setRowNumber(rowNumber);
            seat.setColumnNumber(columnNumber);
            seat.setDeleted(deleted);
            seat.setSector(sector);
            seat.setRoom(room);
            return seat;
        }
    }
}
