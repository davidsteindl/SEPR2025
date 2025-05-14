package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;

@Entity
@DiscriminatorValue("SEATED")
public class SeatedSector extends Sector {

    @OneToMany(
        mappedBy = "sector",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Seat> seats = new ArrayList<>();



    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats.clear();
        if (seats != null) {
            seats.forEach(this::addSeat);
        }
    }

    public void addSeat(Seat seat) {
        seat.setSector(this);
        this.seats.add(seat);
    }

    public void removeSeat(Seat seat) {
        seat.setSector(null);
        this.seats.remove(seat);
    }



    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), seats);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SeatedSector)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SeatedSector that = (SeatedSector) o;
        return Objects.equals(seats, that.seats);
    }

    @Override
    public String toString() {
        return "SeatedSector{"
               + "id=" + getId()
               + ", type=" + getType()
               + ", price=" + getPrice()
               + ", seats=" + seats
               + '}';
    }


    
    public static final class SeatedSectorBuilder {
        private Long id;
        private int price;
        private Room room;
        private List<Seat> seats = new ArrayList<>();

        private SeatedSectorBuilder() { }

        public static SeatedSectorBuilder aSeatedSector() {
            return new SeatedSectorBuilder();
        }

        public SeatedSectorBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SeatedSectorBuilder price(int price) {
            this.price = price;
            return this;
        }

        public SeatedSectorBuilder room(Room room) {
            this.room = room;
            return this;
        }

        public SeatedSectorBuilder seats(List<Seat> seats) {
            this.seats = seats;
            return this;
        }

        public SeatedSector build() {
            SeatedSector sector = new SeatedSector();
            sector.setId(id);
            sector.setType(SectorType.SEATED);
            sector.setPrice(price);
            sector.setRoom(room);
            sector.setSeats(seats);
            return sector;
        }
    }
}