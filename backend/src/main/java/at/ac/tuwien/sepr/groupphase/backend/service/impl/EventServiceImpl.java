package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventRepository eventRepository;
    private final EventLocationRepository eventLocationRepository;
    private final ShowRepository showRepository;
    private final EventMapper eventMapper;
    private final ShowMapper showMapper;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository,
                            EventLocationRepository eventLocationRepository,
                            ShowRepository showRepository,
                            EventMapper eventMapper,
                            ShowMapper showMapper) {
        this.eventRepository = eventRepository;
        this.eventLocationRepository = eventLocationRepository;
        this.showRepository = showRepository;
        this.eventMapper = eventMapper;
        this.showMapper = showMapper;
    }

    @Override
    public Event getEventById(Long id) {
        LOGGER.info("Find event with id {}", id);
        return eventRepository.findById(id).orElse(null);
    }

    @Override
    public List<Event> getAllEvents() {
        LOGGER.info("Get all events");
        return eventRepository.findAll();
    }

    @Override
    public Event createEvent(Event event) throws ValidationException {
        LOGGER.info("Save event {}", event);

        if (event.getLocation() != null) {
            EventLocation location = eventLocationRepository.findById(event.getLocation().getId())
                .orElseThrow(() -> new ValidationException("No event location given", List.of("No event location given")));
            event.setLocation(location);
        } else {
            throw new ValidationException("No event location given", List.of("No event location given"));
        }
        return eventRepository.save(event);
    }

    @Override
    public Page<EventDetailDto> getEventsByArtist(Long artistId, Pageable pageable) {
        LOGGER.info("Fetching events for artistId={} with pageable={}", artistId, pageable);
        return showRepository.findEventsByArtistId(artistId, pageable)
            .map(eventMapper::eventToEventDetailDto);
    }

    @Override
    public Page<ShowDetailDto> getPaginatedShowsForEvent(Long eventId, Pageable pageable) {
        LOGGER.info("Fetching paginated shows for eventId={} with pageable={}", eventId, pageable);

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        return showRepository.findByEvent(event, pageable)
            .map(showMapper::showToShowDetailDto);
    }

}
