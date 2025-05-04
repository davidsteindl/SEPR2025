package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.Objects;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private EventLocation location;

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

    public EventCategory getCategory() {
        return category;
    }

    public void setCategory(EventCategory category) {
        this.category = category;
    }

    public EventLocation getLocation() {
        return location;
    }

    public void setLocation(EventLocation location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event event)) {
            return false;
        }
        return Objects.equals(id, event.id)
            && Objects.equals(name, event.name)
            && category == event.category
            && Objects.equals(location, event.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, location);
    }

    @Override
    public String toString() {
        return "Event{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", category='" + (category != null ? category.getDisplayName() : "null") + '\''
            + ", location='" + (location != null ? location : "null") + '\''
            + '}';
    }

    public enum EventCategory {
        CLASSICAL("Classical"),
        JAZZ("Jazz"),
        ROCK("Rock"),
        POP("Pop"),
        ELECTRONIC("Electronic"),
        HIPHOP("Hip-Hop"),
        COUNTRY("Country"),
        REGGAE("Reggae"),
        FOLK("Folk"),
        OPERA("Opera"),
        MUSICAL("Musical"),
        ALTERNATIVE("Alternative"),
        LATIN("Latin"),
        RNB("R&B"),
        METAL("Metal"),
        INDIE("Indie"),
        THEATRE("Theatre"),
        COMEDY("Comedy"),
        BALLET("Ballet"),
        OTHER("Other");

        private final String displayName;

        EventCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static final class EventBuilder {
        private Long id;
        private String name;
        private EventCategory category;
        private EventLocation location;

        private EventBuilder() {
        }

        public static EventBuilder anEvent() {
            return new EventBuilder();
        }

        public EventBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public EventBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public EventBuilder withCategory(EventCategory category) {
            this.category = category;
            return this;
        }

        public EventBuilder withLocation(EventLocation location) {
            this.location = location;
            return this;
        }

        public Event build() {
            Event event = new Event();
            event.setId(id);
            event.setName(name);
            event.setCategory(category);
            event.setLocation(location);
            return event;
        }
    }
}


