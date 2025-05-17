package at.ac.tuwien.sepr.groupphase.backend.service.specifications;

import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ShowSpecifications {

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

    public static Specification<Show> hasEventId(Long eventId) {
        return (root, query, cb) -> {
            if (eventId == null) return null;
            return cb.equal(root.get("event").get("id"), eventId);
        };
    }

    public static Specification<Show> hasRoomId(Long roomId) {
        return (root, query, cb) -> {
            if (roomId == null) return null;
            return cb.equal(root.get("room").get("id"), roomId);
        };
    }

    public static Specification<Show> nameContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
        };
    }

}
