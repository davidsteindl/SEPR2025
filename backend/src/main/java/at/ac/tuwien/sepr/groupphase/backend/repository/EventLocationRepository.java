package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLocationRepository extends JpaRepository<EventLocation, Long> {
    /**
     * Finds an event location by its ID.
     *
     * @param id the ID of the event location
     * @return the event location with the specified ID
     */
    EventLocation findById(long id);
}