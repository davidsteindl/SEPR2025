// Sector.java
package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;
import java.util.Objects;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;

@Entity
@Table(name = "sectors")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "sector_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sector_type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private SectorType type;

    @Column(nullable = false)
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SectorType getType() {
        return type;
    }

    protected void setType(SectorType type) {
        this.type = type;
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

    @Override
    public int hashCode() {
        return Objects.hash(id, type, price, room);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sector)) {
            return false;
        }
        Sector that = (Sector) o;
        return price == that.price
               && Objects.equals(id, that.id)
               && type == that.type
               && Objects.equals(room, that.room);
    }

    @Override
    public String toString() {
        return "Sector{"
               + "id=" + id
               + ", type=" + type
               + ", price=" + price
               + ", roomId=" + (room != null ? room.getId() : null)
               + '}';
    }
}