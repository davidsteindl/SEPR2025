package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sector> sectors = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "event_location_id", nullable = false)
    private EventLocation eventLocation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Sector> getSectors() {
        return sectors;
    }

    public void setSectors(List<Sector> sectors) {
        this.sectors = sectors;
    }

    public void addSector(Sector sector) {
        sector.setRoom(this);
        this.sectors.add(sector);
    }

    public void removeSector(Sector sector) {
        sector.setRoom(null);
        this.sectors.remove(sector);
    }

    public void addSeat(Seat seat) {
        seat.setRoom(this);
        this.seats.add(seat);
    }

    public void removeSeat(Seat seat) {
        seat.setRoom(null);
        this.seats.remove(seat);
    }


    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public EventLocation getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(EventLocation eventLocation) {
        this.eventLocation = eventLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Room room)) {
            return false;
        }
        return Objects.equals(id, room.id)
            && Objects.equals(name, room.name)
            && Objects.equals(eventLocation, room.eventLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, eventLocation);
    }

    @Override
    public String toString() {
        return "Room{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", eventLocation ID=" + (eventLocation != null ? eventLocation.getId() : "null")
            + ", sectors=" + (sectors != null ? sectors.size() : 0)
            + ", seats=" + (seats != null ? seats.size() : 0)
            + '}';
    }

    public static final class RoomBuilder {
        private String name;
        private List<Sector> sectors = new ArrayList<>();
        private List<Seat> seats = new ArrayList<>();
        private EventLocation eventLocation;

        private RoomBuilder() {
        }

        public static RoomBuilder aRoom() {
            return new RoomBuilder();
        }

        public RoomBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public RoomBuilder withSectors(List<Sector> sectors) {
            this.sectors = sectors;
            return this;
        }

        public RoomBuilder withSeats(List<Seat> seats) {
            this.seats = seats;
            return this;
        }

        public RoomBuilder withEventLocation(EventLocation eventLocation) {
            this.eventLocation = eventLocation;
            return this;
        }

        public Room build() {
            Room room = new Room();
            room.setName(name);
            room.setSectors(sectors);
            room.setSeats(seats);
            room.setEventLocation(eventLocation);
            return room;
        }
    }
}
