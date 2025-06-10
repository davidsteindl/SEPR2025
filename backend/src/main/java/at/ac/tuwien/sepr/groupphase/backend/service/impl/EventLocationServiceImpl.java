package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EventLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class EventLocationServiceImpl implements EventLocationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventLocationRepository eventLocationRepository;
    private final ShowRepository showRepository;
    private final ShowMapper showMapper;

    @Autowired
    public EventLocationServiceImpl(EventLocationRepository eventLocationRepository, ShowRepository showRepository, ShowMapper showMapper) {
        this.eventLocationRepository = eventLocationRepository;
        this.showRepository = showRepository;
        this.showMapper = showMapper;
    }

    @Override
    public EventLocation getEventLocationById(Long eventId) {
        LOGGER.debug("Find event location with id {}", eventId);
        return eventLocationRepository.findById(eventId).orElse(null);
    }

    @Override
    public List<EventLocation> getAllEventLocations() {
        LOGGER.debug("Get all event locations");
        return eventLocationRepository.findAll();
    }

    @Override
    public EventLocation createEventLocation(EventLocation eventLocation) {
        LOGGER.debug("Save event location {}", eventLocation);
        return eventLocationRepository.save(eventLocation);
    }

    @Override
    public Page<ShowDetailDto> getShowsForEventLocation(Long eventLocationId, Pageable pageable) {
        LOGGER.debug("Fetching paginated shows for locationId={} pageable={}", eventLocationId, pageable);

        eventLocationRepository.findById(eventLocationId)
            .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + eventLocationId));

        return showMapper.pageToDto(
            showRepository.findAllByEvent_Location_IdOrderByDateAsc(eventLocationId, pageable)
        );
    }
}
