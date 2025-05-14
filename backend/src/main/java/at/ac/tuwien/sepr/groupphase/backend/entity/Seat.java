package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Column(nullable = false)
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private SeatedSector sector;


    
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public SeatedSector getSector() {
        return sector;
    }

    public void setSector(SeatedSector sector) {
        this.sector = sector;
    }


    
    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            rowNumber,
            deleted,
            sector != null ? sector.getId() : null
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Seat)) {
            return false;
        }
        Seat that = (Seat) o;
        return rowNumber == that.rowNumber
               && deleted == that.deleted
               && Objects.equals(id, that.id)
               && Objects.equals(
                   sector != null ? sector.getId() : null,
                   that.sector != null ? that.sector.getId() : null
               );
    }

    @Override
    public String toString() {
        return "Seat{"
               + "id=" + id
               + ", rowNumber=" + rowNumber
               + ", deleted=" + deleted
               + ", sectorId=" + (sector != null ? sector.getId() : null)
               + '}';
    }


    
    public static final class SeatBuilder {
        private Long id;
        private int rowNumber;
        private boolean deleted;
        private SeatedSector sector;

        private SeatBuilder() { }

        public static SeatBuilder aSeat() {
            return new SeatBuilder();
        }

        public SeatBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SeatBuilder rowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
            return this;
        }

        public SeatBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public SeatBuilder sector(SeatedSector sector) {
            this.sector = sector;
            return this;
        }

        public Seat build() {
            Seat seat = new Seat();
            seat.setId(id);
            seat.setRowNumber(rowNumber);
            seat.setDeleted(deleted);
            seat.setSector(sector);
            return seat;
        }
    }
}