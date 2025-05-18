package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EventLocationSearchDto {

    @NotNull(message = "Page index must not be null")
    @Min(value = 0, message = "Page index must be non-negative")
    private Integer page = 0;

    @NotNull(message = "Page size must not be null")
    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size = 10;

    private String name;

    private String street;

    private String city;

    private String country;

    private String postalCode;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
