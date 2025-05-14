package at.ac.tuwien.sepr.groupphase.backend.service.specifications;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import org.springframework.data.jpa.domain.Specification;


/** * This class contains specifications for filtering events.
 * It provides methods to create specifications based on various criteria such as name, type, description, and duration.
 */
public final class EventSpecifications {
    private EventSpecifications() { }

    /**
     * Creates a specification to filter events by name.
     *
     * @param name the name to filter by
     * @return a specification that filters events by name
     */
    public static Specification<Event> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    /**
     * Creates a specification to filter events by type.
     *
     * @param type the type to filter by
     * @return a specification that filters events by type
     */
    public static Specification<Event> hasType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("category").as(String.class)),
                "%" + type.toLowerCase() + "%");
        };
    }

    /**
     * Creates a specification to filter events by description.
     *
     * @param description the description to filter by
     * @return a specification that filters events by description
     */
    public static Specification<Event> hasDescription(String description) {
        return (root, query, cb) -> {
            if (description == null || description.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("description")),
                "%" + description.toLowerCase() + "%");
        };
    }

    /**
     * Creates a specification to filter events by duration.
     *
     * @param wanted the desired duration
     * @return a specification that filters events by duration
     */
    public static Specification<Event> hasDurationBetween(int wanted) {

        return (root, query, cb) -> cb.between(
        root.get("totalDuration").as(Integer.class),
        wanted - 30,
        wanted + 30
    );
    }
}
