package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive(message = "Price must be positive")
    private int price;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean isBookable() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sector sector)) {
            return false;
        }
        return price == sector.price
            && Objects.equals(id, sector.id)
            && Objects.equals(room, sector.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, room);
    }

    @Override
    public String toString() {
        return "Sector{"
            + "id=" + id
            + ", price=" + price
            + ", room ID=" + (room != null ? room.getId() : "null")
            + '}';
    }

    public static final class SectorBuilder {
        private int price;
        private Room room;

        private SectorBuilder() {
        }

        public static SectorBuilder aSector() {
            return new SectorBuilder();
        }

        public SectorBuilder withPrice(int price) {
            this.price = price;
            return this;
        }

        public SectorBuilder withRoom(Room room) {
            this.room = room;
            return this;
        }

        public Sector build() {
            Sector sector = new Sector();
            sector.setPrice(price);
            sector.setRoom(room);
            return sector;
        }
    }
}
