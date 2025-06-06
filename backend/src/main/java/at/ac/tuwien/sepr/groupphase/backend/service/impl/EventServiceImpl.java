package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventCategoryDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventTopTenDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EventMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShowMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EventService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.EventValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventRepository eventRepository;
    private final EventLocationRepository eventLocationRepository;
    private final ShowRepository showRepository;
    private final TicketRepository ticketRepository;
    private final EventMapper eventMapper;
    private final ShowMapper showMapper;
    private final EventValidator eventValidator;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository,
                            EventLocationRepository eventLocationRepository,
                            ShowRepository showRepository,
                            TicketRepository ticketRepository,
                            EventMapper eventMapper,
                            ShowMapper showMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.eventLocationRepository = eventLocationRepository;
        this.showRepository = showRepository;
        this.ticketRepository = ticketRepository;
        this.eventMapper = eventMapper;
        this.showMapper = showMapper;
        this.eventValidator = eventValidator;
    }

    @Override
    public Event getEventById(Long id) {
        LOGGER.debug("Find event with id {}", id);
        return eventRepository.findById(id).orElse(null);
    }

    @Override
    public List<Event> getAllEvents() {
        LOGGER.debug("Get all events");
        return eventRepository.findAll();
    }

    @Override
    public Event createEvent(Event event) throws ValidationException {
        LOGGER.debug("Save event {}", event);

        eventValidator.validateForCreate(event);
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
    public Event updateEvent(Long id, Event event) throws ValidationException {
        LOGGER.debug("Update event {} with data {}", id, event);

        eventValidator.validateForUpdate(id, event);

        Event existing = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Event not found with id: " + id));

        EventLocation location = eventLocationRepository.findById(event.getLocation().getId())
            .orElseThrow(() -> new ValidationException("No event location given", List.of("No event location given")));

        existing.setName(event.getName());
        existing.setCategory(event.getCategory());
        existing.setDescription(event.getDescription());
        existing.setDateTime(event.getDateTime());
        existing.setDuration(event.getDuration());
        existing.setLocation(location);

        return eventRepository.save(existing);
    }

    @Override
    public Page<EventDetailDto> getEventsByArtist(Long artistId, Pageable pageable) {
        LOGGER.debug("Fetching events for artistId={} with pageable={}", artistId, pageable);
        return showRepository.findEventsByArtistId(artistId, pageable)
            .map(eventMapper::eventToEventDetailDto);
    }

    @Override
    public Page<ShowDetailDto> getPaginatedShowsForEvent(Long eventId, Pageable pageable) {
        LOGGER.debug("Fetching paginated shows for eventId={} with pageable={}", eventId, pageable);

        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        return showRepository.findByEvent(event, pageable)
            .map(showMapper::showToShowDetailDto);
    }

    @Override
    public List<EventTopTenDto> getTopTenEventsByCategory(String category) throws ValidationException {
        LOGGER.debug("Fetching top ten events for category={}", category);
        Pageable topTen = PageRequest.of(0, 10);
        List<Object[]> topTenEvents;
        if (category.equalsIgnoreCase("all")) {
            topTenEvents = ticketRepository.findTopTenEventsOrderByTicketCountDesc(topTen);
        } else {
            try {
                Event.EventCategory.valueOf(category);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid category: " + category, List.of("Invalid category: " + category));
            }
            Event.EventCategory eventCategory = Event.EventCategory.valueOf(category.toUpperCase());
            topTenEvents = ticketRepository.findTopTenEventsByCategoryOrderByTicketCountDesc(eventCategory, topTen);
        }

        List<EventTopTenDto> eventTopTenDtos = new ArrayList<>();

        for (Object[] objects : topTenEvents) {
            Event event = (Event) objects[0];
            Long ticketCount = (Long) objects[1];

            LocalDateTime date = event.getDateTime();

            EventTopTenDto eventTopTenDto = EventTopTenDto.EventTopTenDtoBuilder.anEventTopTenDto()
                .id(event.getId())
                .name(event.getName())
                .date(date)
                .ticketsSold(ticketCount)
                .build();

            eventTopTenDtos.add(eventTopTenDto);
        }

        return eventTopTenDtos;
    }

    @Override
    public List<EventCategoryDto> getAllEventCategories() {
        LOGGER.debug("Fetching all event categories");
        return Arrays.stream(Event.EventCategory.values())
            .map(cat -> EventCategoryDto.EventCategoryDtoBuilder
                .anEventCategoryDto()
                .name(cat.name())
                .displayName(cat.getDisplayName())
                .build())
            .toList();
    }
}
