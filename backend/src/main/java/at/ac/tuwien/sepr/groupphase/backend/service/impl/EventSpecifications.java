package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import org.springframework.data.jpa.domain.Specification;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;


public final class EventSpecifications {
    private EventSpecifications() { }

    public static Specification<Event> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Event> hasType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("category").as(String.class)),
                "%" + type.toLowerCase() + "%");
        };
    }

    public static Specification<Event> hasDescription(String description) {
        return (root, query, cb) -> {
            if (description == null || description.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("description")),
                "%" + description.toLowerCase() + "%");
        };
    }

    public static Specification<Event> hasDurationBetween(int wanted) {

        return (root, query, cb) -> cb.between(
        root.get("totalDuration").as(Integer.class),
        wanted - 30,
        wanted + 30
    );
    }
}
