package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    /**
     * Finds all events in the persistent datastore based on their category.
     *
     * @param category the category of the event
     * @return the events associated with the provided category
     */
    List<Event> findAllByCategory(Event.EventCategory category);
}
