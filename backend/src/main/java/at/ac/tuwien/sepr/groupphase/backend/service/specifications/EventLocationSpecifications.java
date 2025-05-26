package at.ac.tuwien.sepr.groupphase.backend.service.specifications;

import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import org.springframework.data.jpa.domain.Specification;

/**
 * This class contains specifications for filtering event locations.
 * It provides methods to create specifications based on various criteria such as name, street, city, country, and postal code.
 */
public final class EventLocationSpecifications {
    private EventLocationSpecifications() { }


    /**
     * Creates a specification to filter event locations by name.
     *
     * @param name the name to filter by
     * @return a specification that filters event locations by name
     */
    public static Specification<EventLocation> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    /**
     * Creates a specification to filter event locations by street.
     *
     * @param street the street to filter by
     * @return a specification that filters event locations by street
     */
    public static Specification<EventLocation> hasStreetLike(String street) {
        return (root, query, cb) -> {
            if (street == null || street.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("street")), "%" + street.toLowerCase() + "%");
        };
    }

    /**
     * Creates a specification to filter event locations by city.
     *
     * @param city the city to filter by
     * @return a specification that filters event locations by city
     */
    public static Specification<EventLocation> hasCityLike(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
        };
    }

    /**
     * Creates a specification to filter event locations by country.
     *
     * @param country the country to filter by
     * @return a specification that filters event locations by country
     */
    public static Specification<EventLocation> hasCountryLike(String country) {
        return (root, query, cb) -> {
            if (country == null || country.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("country")), "%" + country.toLowerCase() + "%");
        };
    }

    /**
     * Creates a specification to filter event locations by postal code of their city.
     *
     * @param postalCode the postal code to filter by
     * @return a specification that filters event locations by postal code
     */
    public static Specification<EventLocation> hasPostalCodeLike(String postalCode) {
        return (root, query, cb) -> {
            if (postalCode == null || postalCode.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("postalCode")), "%" + postalCode.toLowerCase() + "%");
        };
    }

}
