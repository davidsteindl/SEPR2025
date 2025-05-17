package at.ac.tuwien.sepr.groupphase.backend.service.specifications;

import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Provides reusable JPA Specifications for dynamic querying of Show entities.
 */
public final class ShowSpecifications {

    /**
     * Filters shows whose date is between the specified start and end (inclusive).
     * Supports open-ended ranges (start only, end only, or both).
     */
    public static Specification<Show> dateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(root.get("date"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), start);
            } else if (end != null) {
                return cb.lessThanOrEqualTo(root.get("date"), end);
            }
            return null;
        };
    }

    /**
     * Filters shows by a case-insensitive partial match on the associated event's name.
     */
    public static Specification<Show> hasEventName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("event").get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    /**
     * Filters shows by a case-insensitive partial match on the associated room's name.
     */
    public static Specification<Show> hasRoomName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("room").get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    /**
     * Filters shows by a case-insensitive partial match on the show's own name.
     */
    public static Specification<Show> nameContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
        };
    }

}
