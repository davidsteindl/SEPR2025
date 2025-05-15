package at.ac.tuwien.sepr.groupphase.backend.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Column(name = "column_number", nullable = false)
    private int columnNumber;

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

    public SeatedSector getSector() {
        return sector;
    }

    public void setSector(SeatedSector sector) {
        this.sector = sector;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rowNumber, columnNumber, deleted, sector != null ? sector.getId() : null);
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
            && columnNumber == that.columnNumber
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
            + ", columnNumber=" + columnNumber
            + ", deleted=" + deleted
            + ", sectorId=" + (sector != null ? sector.getId() : null)
            + '}';
    }
}
