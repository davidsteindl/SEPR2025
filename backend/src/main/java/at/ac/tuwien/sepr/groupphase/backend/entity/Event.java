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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Min(10)
    @Max(10000)
    @Column(nullable = false)
    private int duration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private EventLocation location;

    @PrePersist
    @PreUpdate
    private void truncateDateTimeToMinutes() {
        if (this.dateTime != null) {
            this.dateTime = this.dateTime.truncatedTo(ChronoUnit.MINUTES);
        }
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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
        return duration == event.duration
            && Objects.equals(id, event.id)
            && Objects.equals(name, event.name)
            && category == event.category
            && Objects.equals(description, event.description)
            && Objects.equals(dateTime, event.dateTime)
            && Objects.equals(location, event.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, description, dateTime, duration, location);
    }

    @Override
    public String toString() {
        return "Event{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", category='" + (category != null ? category.getDisplayName() : "null") + '\''
            + ", description='" + description + '\''
            + ", dateTime=" + dateTime
            + ", duration=" + duration
            + ", location ID='" + (location != null ? location.getId() : "null") + '\''
            + ", description='" + (description != null ? description : "null") + '\''
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
        private String name;
        private EventCategory category;
        private String description;
        private LocalDateTime dateTime;
        private int duration;
        private EventLocation location;

        private EventBuilder() {
        }

        public static EventBuilder anEvent() {
            return new EventBuilder();
        }

        public EventBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public EventBuilder withCategory(EventCategory category) {
            this.category = category;
            return this;
        }

        public EventBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public EventBuilder withDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public EventBuilder withDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public EventBuilder withLocation(EventLocation location) {
            this.location = location;
            return this;
        }

        public Event build() {
            Event event = new Event();
            event.setName(name);
            event.setCategory(category);
            event.setDescription(description);
            event.setDateTime(dateTime);
            event.setDuration(duration);
            event.setLocation(location);
            return event;
        }
    }
}
