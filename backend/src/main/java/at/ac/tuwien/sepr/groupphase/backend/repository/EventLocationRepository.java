package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventLocationRepository extends JpaRepository<EventLocation, Long> {
    /**
     * Finds an event location by its name.
     *
     * @param name the name of the event location
     * @return an Optional containing the found event location, or empty if no event location was found
     */
    Optional<EventLocation> findByName(String name);
}