package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Profile("generateData")
@Component
public class EventDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EventRepository eventRepository;
    private final EventLocationRepository eventLocationRepository;

    private static final int NUMBER_OF_EVENTS_TO_GENERATE = 5;
    private static final int NUMBER_OF_LOCATIONS_TO_GENERATE = 5;

    @Autowired
    public EventDataGenerator(EventRepository eventRepository, EventLocationRepository eventLocationRepository) {
        this.eventRepository = eventRepository;
        this.eventLocationRepository = eventLocationRepository;
    }

    @PostConstruct
    private void generateEvents() {
        if (!eventLocationRepository.findAll().isEmpty()) {
            LOGGER.debug("locations already generated");
        } else {
            LOGGER.debug("generating {} EventLocation entries", NUMBER_OF_LOCATIONS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_LOCATIONS_TO_GENERATE; i++) {
                EventLocation eventLocation = EventLocation.EventLocationBuilder.anEventLocation()
                    .withId((long) i - NUMBER_OF_LOCATIONS_TO_GENERATE - 1)
                    .withName("Location " + i)
                    .withType(EventLocation.LocationType.OPERA)
                    .withCountry("Country " + i)
                    .withCity("City " + i)
                    .withStreet("Street " + i)
                    .withPostalCode("PostalCode " + i)
                    .build();
                LOGGER.debug("saving event location {}", eventLocation);
                eventLocationRepository.save(eventLocation);
            }
        }

        if (!eventRepository.findAll().isEmpty()) {
            LOGGER.debug("events already generated");
        } else {
            LOGGER.debug("generating {} Event entries", NUMBER_OF_EVENTS_TO_GENERATE);
            for (int i = 0; i < NUMBER_OF_EVENTS_TO_GENERATE; i++) {
                EventLocation eventLocation = eventLocationRepository.findById((long) i - NUMBER_OF_LOCATIONS_TO_GENERATE - 1);
                if (eventLocation != null) {
                    Event event = Event.EventBuilder.anEvent()
                        .withName("Event " + i)
                        .withCategory(Event.EventCategory.CLASSICAL)
                        .withLocation(eventLocation)
                        .build();
                    LOGGER.debug("saving event {}", event);
                    eventRepository.save(event);
                }
            }
        }
    }
}
