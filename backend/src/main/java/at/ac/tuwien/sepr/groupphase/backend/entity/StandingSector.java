package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;
import java.util.Objects;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;

@Entity
@DiscriminatorValue("STANDING")
public class StandingSector extends Sector {

    @Column(nullable = false)
    private int capacity;



    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }



    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), capacity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StandingSector)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        StandingSector that = (StandingSector) o;
        return capacity == that.capacity;
    }

    @Override
    public String toString() {
        return "StandingSector{"
               + "id=" + getId()
               + ", type=" + getType()
               + ", price=" + getPrice()
               + ", capacity=" + capacity
               + '}';
    }


    
    public static final class StandingSectorBuilder {
        private Long id;
        private int price;
        private Room room;
        private int capacity;

        private StandingSectorBuilder() { }

        public static StandingSectorBuilder aStandingSector() {
            return new StandingSectorBuilder();
        }

        public StandingSectorBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public StandingSectorBuilder price(int price) {
            this.price = price;
            return this;
        }

        public StandingSectorBuilder room(Room room) {
            this.room = room;
            return this;
        }

        public StandingSectorBuilder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public StandingSector build() {
            StandingSector sector = new StandingSector();
            sector.setId(id);
            sector.setType(SectorType.STANDING);
            sector.setPrice(price);
            sector.setRoom(room);
            sector.setCapacity(capacity);
            return sector;
        }
    }
}
