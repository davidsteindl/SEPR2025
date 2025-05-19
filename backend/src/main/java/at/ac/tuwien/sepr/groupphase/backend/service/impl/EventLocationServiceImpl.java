package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EventLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class EventLocationServiceImpl implements EventLocationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventLocationRepository eventLocationRepository;
    private final ShowRepository showRepository;

    @Autowired
    public EventLocationServiceImpl(EventLocationRepository eventLocationRepository, ShowRepository showRepository) {
        this.eventLocationRepository = eventLocationRepository;
        this.showRepository = showRepository;
    }

    @Override
    public EventLocation getEventLocationById(Long eventId) {
        LOGGER.info("Find event location with id {}", eventId);
        return eventLocationRepository.findById(eventId).orElse(null);
    }

    @Override
    public List<EventLocation> getAllEventLocations() {
        LOGGER.info("Get all event locations");
        return eventLocationRepository.findAll();
    }

    @Override
    public EventLocation createEventLocation(EventLocation eventLocation) {
        LOGGER.info("Save event location {}", eventLocation);
        return eventLocationRepository.save(eventLocation);
    }

    @Override
    public List<Show> getShowsForEventLocation(Long eventLocationId) {
        LOGGER.info("Get all shows for event location {}", eventLocationId);
        return showRepository.findAllByEvent_Location_IdOrderByDateAsc(eventLocationId);
    }
}
