package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;

public class EventLocationDto {
    @NotNull(message = "ID must not be null")
    private Long id;
    @NotNull(message = "Name must not be null")
    private String name;
    @NotNull(message = "Type must not be null")
    private String type;
    @NotNull(message = "Country must not be null")
    private String country;
    @NotNull(message = "City must not be null")
    private String city;
    @NotNull(message = "Street must not be null")
    private String street;
    @NotNull(message = "Postal code must not be null")
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
        if (!(o instanceof EventLocationDto that)) {
            return false;
        }
        return id.equals(that.id)
            && name.equals(that.name)
            && type.equals(that.type)
            && country.equals(that.country)
            && city.equals(that.city)
            && street.equals(that.street)
            && postalCode.equals(that.postalCode);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, type, country, city, street, postalCode);
    }

    @Override
    public String toString() {
        return "EventLocationDto{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", type='" + type + '\''
            + ", country='" + country + '\''
            + ", city='" + city + '\''
            + ", street='" + street + '\''
            + ", postalCode='" + postalCode + '\''
            + '}';
    }

    public static final class EventLocationDtoBuilder {
        private Long id;
        private String name;
        private String type;
        private String country;
        private String city;
        private String street;
        private String postalCode;

        private EventLocationDtoBuilder() {
        }

        public static EventLocationDtoBuilder anEventLocationDto() {
            return new EventLocationDtoBuilder();
        }

        public EventLocationDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EventLocationDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EventLocationDtoBuilder type(String type) {
            this.type = type;
            return this;
        }

        public EventLocationDtoBuilder country(String country) {
            this.country = country;
            return this;
        }

        public EventLocationDtoBuilder city(String city) {
            this.city = city;
            return this;
        }

        public EventLocationDtoBuilder street(String street) {
            this.street = street;
            return this;
        }

        public EventLocationDtoBuilder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public EventLocationDto build() {
            EventLocationDto eventLocationDto = new EventLocationDto();
            eventLocationDto.setId(id);
            eventLocationDto.setName(name);
            eventLocationDto.setType(type);
            eventLocationDto.setCountry(country);
            eventLocationDto.setCity(city);
            eventLocationDto.setStreet(street);
            eventLocationDto.setPostalCode(postalCode);
            return eventLocationDto;
        }
    }
}
