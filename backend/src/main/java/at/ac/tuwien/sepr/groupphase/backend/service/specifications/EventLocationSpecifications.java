package at.ac.tuwien.sepr.groupphase.backend.service.specifications;

import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import org.springframework.data.jpa.domain.Specification;

public final class EventLocationSpecifications {
    private EventLocationSpecifications() { }

    public static Specification<EventLocation> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<EventLocation> hasStreetLike(String street) {
        return (root, query, cb) -> {
            if (street == null || street.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("street")), "%" + street.toLowerCase() + "%");
        };
    }

    public static Specification<EventLocation> hasCityLike(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
        };
    }

    public static Specification<EventLocation> hasCountryLike(String country) {
        return (root, query, cb) -> {
            if (country == null || country.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("country")), "%" + country.toLowerCase() + "%");
        };
    }

    public static Specification<EventLocation> hasPostalCodeLike(String postalCode) {
        return (root, query, cb) -> {
            if (postalCode == null || postalCode.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("postalCode")), "%" + postalCode.toLowerCase() + "%");
        };
    }

}
