package at.ac.tuwien.sepr.groupphase.backend.service.specifications;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import org.springframework.data.jpa.domain.Specification;

/**
 * This class contains specifications for filtering artists.
 * It provides methods to create specifications based on various criteria such as firstname, lastname, and stagename.
 */
public final class ArtistSpecifications {

    private ArtistSpecifications() { }

    /**
     * Creates a specification to filter artists by first name.
     *
     * @param firstname the first name to filter by
     * @return a specification that filters artists by first name
     */
    public static Specification<Artist> hasFirstnameLike(String firstname) {
        return (root, query, cb) -> {
            if (firstname == null || firstname.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("firstname")), "%" + firstname.toLowerCase() + "%");
        };
    }

    /**
     * Creates a specification to filter artists by last name.
     *
     * @param lastname the last name to filter by
     * @return a specification that filters artists by last name
     */
    public static Specification<Artist> hasLastnameLike(String lastname) {
        return (root, query, cb) -> {
            if (lastname == null || lastname.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("lastname")), "%" + lastname.toLowerCase() + "%");
        };
    }

    /**
     * Creates a specification to filter artists by stage name.
     *
     * @param stagename the stage name to filter by
     * @return a specification that filters artists by stage name
     */
    public static Specification<Artist> hasStagenameLike(String stagename) {
        return (root, query, cb) -> {
            if (stagename == null || stagename.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("stagename")), "%" + stagename.toLowerCase() + "%");
        };
    }
}