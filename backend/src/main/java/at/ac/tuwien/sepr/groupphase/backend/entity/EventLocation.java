package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
public class EventLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType type;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String street;

    @Column(nullable = false, length = 100)
    private String postalCode;

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

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventLocation eventLocation)) {
            return false;
        }
        return Objects.equals(id, eventLocation.id)
            && Objects.equals(name, eventLocation.name)
            && type == eventLocation.type
            && Objects.equals(country, eventLocation.country)
            && Objects.equals(city, eventLocation.city)
            && Objects.equals(street, eventLocation.street)
            && Objects.equals(postalCode, eventLocation.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, country, city, street, postalCode);
    }

    @Override
    public String toString() {
        return "EventLocation{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", type=" + (type != null ? type.getDisplayName() : "null")
            + ", country='" + country + '\''
            + ", city='" + city + '\''
            + ", street='" + street + '\''
            + ", postalCode='" + postalCode + '\''
            + '}';
    }

    public enum LocationType {
        STADIUM("Stadium"),
        FESTIVAL_GROUND("Festival Ground"),
        HALL("Hall"),
        OPERA("Opera"),
        THEATER("Theater"),
        CLUB("Club"),
        OTHER("Other");

        private final String displayName;

        LocationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static final class EventLocationBuilder {
        private String name;
        private LocationType type;
        private String country;
        private String city;
        private String street;
        private String postalCode;

        private EventLocationBuilder() {
        }

        public static EventLocationBuilder anEventLocation() {
            return new EventLocationBuilder();
        }

        public EventLocationBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public EventLocationBuilder withType(LocationType type) {
            this.type = type;
            return this;
        }

        public EventLocationBuilder withCountry(String country) {
            this.country = country;
            return this;
        }

        public EventLocationBuilder withCity(String city) {
            this.city = city;
            return this;
        }

        public EventLocationBuilder withStreet(String street) {
            this.street = street;
            return this;
        }

        public EventLocationBuilder withPostalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public EventLocation build() {
            EventLocation eventLocation = new EventLocation();
            eventLocation.setName(name);
            eventLocation.setType(type);
            eventLocation.setCountry(country);
            eventLocation.setCity(city);
            eventLocation.setStreet(street);
            eventLocation.setPostalCode(postalCode);
            return eventLocation;
        }
    }
}
