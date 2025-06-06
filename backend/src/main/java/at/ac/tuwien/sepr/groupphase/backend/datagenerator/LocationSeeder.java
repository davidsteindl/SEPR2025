package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a list of EventLocations and saves them to the database.
 * This class is only active when the "generateData" profile is active.
 */
@Component("locationSeeder")
@Profile("generateData")
public class LocationSeeder {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationSeeder.class);
    private static final int NUMBER_OF_LOCATIONS = 25;

    private final EventLocationRepository locationRepository;

    public LocationSeeder(EventLocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @PostConstruct
    public void generateLocations() {
        if (locationRepository.count() > 0) {
            LOGGER.debug("EventLocations already generated, skipping LocationSeeder");
            return;
        }
        List<EventLocation> list = new ArrayList<>();
        LOGGER.debug("Generating {} EventLocations", NUMBER_OF_LOCATIONS);
        for (int i = 0; i < NUMBER_OF_LOCATIONS; i++) {
            EventLocation loc = EventLocation.EventLocationBuilder.anEventLocation()
                .withName("Location " + i)
                .withType(EventLocation.LocationType.OPERA)
                .withCountry("Country " + i)
                .withCity("City " + i)
                .withStreet("Street " + i)
                .withPostalCode("PostalCode " + i)
                .build();
            list.add(loc);
        }
        locationRepository.saveAll(list);
        LOGGER.debug("Saved {} EventLocations", list.size());
    }
}