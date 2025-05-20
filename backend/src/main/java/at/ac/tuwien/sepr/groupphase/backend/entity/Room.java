package at.ac.tuwien.sepr.groupphase.backend.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(
        mappedBy = "room",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Sector> sectors = new ArrayList<>();

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
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

    public EventLocation getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(EventLocation eventLocation) {
        this.eventLocation = eventLocation;
    }

    public List<Sector> getSectors() {
        return sectors;
    }

    public void setSectors(List<Sector> sectors) {
        this.sectors.clear();
        if (sectors != null) {
            sectors.forEach(this::addSector);
        }
    }

    public void addSector(Sector sector) {
        sector.setRoom(this);
        this.sectors.add(sector);
    }

    public void removeSector(Sector sector) {
        sector.setRoom(null);
        this.sectors.remove(sector);
    }


    public static final class RoomBuilder {
        private Long id;
        private String name;
        private boolean horizontal;
        private EventLocation eventLocation;
        private List<Sector> sectors = new ArrayList<>();

        private RoomBuilder() { }

        public static RoomBuilder aRoom() {
            return new RoomBuilder();
        }

        public RoomBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoomBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomBuilder horizontal(boolean horizontal) {
            this.horizontal = horizontal;
            return this;
        }

        public RoomBuilder eventLocation(EventLocation eventLocation) {
            this.eventLocation = eventLocation;
            return this;
        }

        public RoomBuilder sectors(List<Sector> sectors) {
            this.sectors = sectors;
            return this;
        }

        public Room build() {
            Room room = new Room();
            room.setId(id);
            room.setName(name);
            room.setEventLocation(eventLocation);
            room.setSectors(sectors);
            return room;
        }
    }

}
