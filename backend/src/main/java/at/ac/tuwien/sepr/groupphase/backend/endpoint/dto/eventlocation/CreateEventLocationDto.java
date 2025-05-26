package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateEventLocationDto {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Type must not be blank")
    private String type;

    @NotBlank(message = "Country must not be blank")
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    @NotBlank(message = "City must not be blank")
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @NotBlank(message = "Street must not be blank")
    @Size(max = 50, message = "Street must not exceed 50 characters")
    private String street;

    @NotBlank(message = "Postal code must not be blank")
    @Size(max = 50, message = "Postal code must not exceed 50 characters")
    private String postalCode;

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
    public String toString() {
        return "CreateEventLocationDto{"
            + "name='" + name + '\''
            + ", type='" + type + '\''
            + ", country='" + country + '\''
            + ", city='" + city + '\''
            + ", street='" + street + '\''
            + ", postalCode='" + postalCode + '\''
            + '}';
    }

    public static final class CreateEventLocationDtoBuilder {
        private String name;
        private String type;
        private String country;
        private String city;
        private String street;
        private String postalCode;

        private CreateEventLocationDtoBuilder() {
        }

        public static CreateEventLocationDtoBuilder aCreateEventLocationDto() {
            return new CreateEventLocationDtoBuilder();
        }

        public CreateEventLocationDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CreateEventLocationDtoBuilder type(String type) {
            this.type = type;
            return this;
        }

        public CreateEventLocationDtoBuilder country(String country) {
            this.country = country;
            return this;
        }

        public CreateEventLocationDtoBuilder city(String city) {
            this.city = city;
            return this;
        }

        public CreateEventLocationDtoBuilder street(String street) {
            this.street = street;
            return this;
        }

        public CreateEventLocationDtoBuilder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public CreateEventLocationDto build() {
            CreateEventLocationDto createEventLocationDto = new CreateEventLocationDto();
            createEventLocationDto.setName(name);
            createEventLocationDto.setType(type);
            createEventLocationDto.setCountry(country);
            createEventLocationDto.setCity(city);
            createEventLocationDto.setStreet(street);
            createEventLocationDto.setPostalCode(postalCode);
            return createEventLocationDto;
        }
    }
}
