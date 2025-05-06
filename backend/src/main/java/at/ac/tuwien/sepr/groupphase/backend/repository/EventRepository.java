package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    /**
     * Finds an event by its name.
     *
     * @param name the name of the event
     * @return an Optional containing the found event, or empty if no event was found
     */
    Optional<Event> findByName(String name);
}
