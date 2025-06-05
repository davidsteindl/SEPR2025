package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.util.Objects;

@Entity
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
    public boolean isBookable() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StandingSector that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return capacity == that.capacity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), capacity);
    }

    @Override
    public String toString() {
        return "StandingSector{"
            + "id=" + getId()
            + ", price=" + getPrice()
            + ", room ID=" + (getRoom() != null ? getRoom().getId() : "null")
            + ", capacity=" + capacity
            + '}';
    }

    public static final class StandingSectorBuilder {
        private int price;
        private int capacity;
        private Room room;

        public static StandingSectorBuilder aStandingSector() {
            return new StandingSectorBuilder();
        }

        public StandingSectorBuilder withPrice(int price) {
            this.price = price;
            return this;
        }

        public StandingSectorBuilder withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public StandingSectorBuilder withRoom(Room room) {
            this.room = room;
            return this;
        }

        public StandingSector build() {
            StandingSector standingSector = new StandingSector();
            standingSector.setPrice(price);
            standingSector.setCapacity(capacity);
            standingSector.setRoom(room);
            return standingSector;
        }
    }
}
