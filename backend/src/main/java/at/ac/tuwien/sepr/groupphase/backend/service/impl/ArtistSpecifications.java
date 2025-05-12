package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import org.springframework.data.jpa.domain.Specification;

public class ArtistSpecifications {

    public static Specification<Artist> hasFirstnameLike(String firstname) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("firstname")), "%" + firstname.toLowerCase() + "%");
    }

    public static Specification<Artist> hasLastnameLike(String lastname) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("lastname")), "%" + lastname.toLowerCase() + "%");
    }

    public static Specification<Artist> hasStagenameLike(String stagename) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("stagename")), "%" + stagename.toLowerCase() + "%");
    }
}