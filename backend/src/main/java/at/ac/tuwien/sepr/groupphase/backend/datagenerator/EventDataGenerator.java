package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Generates 200 Events, 500 Shows, and 100 Artists for testing.
 * Each Show is assigned to a random Event and scheduled such that its start + duration
 * lies within the Event's dateTime + duration window. Each Artist is assigned to
 * a few random Shows.
 * This class is only active when the "generateData" profile is active.
 */
@Component("eventDataGenerator")
@Profile("generateData")
@DependsOn("roomDataGenerator")
public class EventDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDataGenerator.class);
    private static final int NUMBER_OF_EVENTS = 200;
    private static final int NUMBER_OF_SHOWS = 500;
    private static final int NUMBER_OF_ARTISTS = 100;
    private static final int MIN_SHOWS_PER_ARTIST = 1;
    private static final int MAX_SHOWS_PER_ARTIST = 20;

    private final EventRepository eventRepository;
    private final ShowRepository showRepository;
    private final ArtistRepository artistRepository;
    private final EventLocationRepository locationRepository;
    private final RoomRepository roomRepository;
    private final Random random = new Random();

    public EventDataGenerator(EventRepository eventRepository,
                              ShowRepository showRepository,
                              ArtistRepository artistRepository,
                              EventLocationRepository locationRepository,
                              RoomRepository roomRepository) {
        this.eventRepository = eventRepository;
        this.showRepository = showRepository;
        this.artistRepository = artistRepository;
        this.locationRepository = locationRepository;
        this.roomRepository = roomRepository;
    }

    @PostConstruct
    public void generateEventsShowsAndArtists() {
        if (eventRepository.count() > 0) {
            LOGGER.debug("Events already generated, skipping EventDataGenerator");
            return;
        }

        List<EventLocation> locations = locationRepository.findAll();
        if (locations.isEmpty()) {
            LOGGER.warn("No EventLocations found: generate EventLocations first");
            return;
        }
        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) {
            LOGGER.warn("No Rooms found: generate Rooms first");
            return;
        }

        List<Event> events = new ArrayList<>(NUMBER_OF_EVENTS);
        Event.EventCategory[] categories = Event.EventCategory.values();

        LOGGER.debug("Generating {} Events", NUMBER_OF_EVENTS);
        for (int i = 0; i < NUMBER_OF_EVENTS; i++) {

            EventLocation loc = locations.get(random.nextInt(locations.size()));

            LocalDateTime eventStart = LocalDateTime.now()
                .plusDays(random.nextInt(90))
                .plusHours(random.nextInt(24))
                .plusMinutes(random.nextInt(60))
                .truncatedTo(ChronoUnit.MINUTES);

            int eventDuration = 180 + random.nextInt(1261);


            Event.EventCategory category = categories[random.nextInt(categories.length)];

            Event event = Event.EventBuilder.anEvent()
                .withName("Event " + i)
                .withCategory(category)
                .withDescription("Description of Event " + i)
                .withDateTime(eventStart)
                .withDuration(eventDuration)
                .withLocation(loc)
                .build();

            events.add(event);
        }

        eventRepository.saveAll(events);
        LOGGER.debug("Saved {} Events", events.size());


        List<Show> shows = new ArrayList<>(NUMBER_OF_SHOWS);
        LOGGER.debug("Generating {} Shows", NUMBER_OF_SHOWS);
        for (int j = 0; j < NUMBER_OF_SHOWS; j++) {
            Event assignedEvent = events.get(random.nextInt(events.size()));
            LocalDateTime eventStart = assignedEvent.getDateTime();
            LocalDateTime eventEnd = eventStart.plusMinutes(assignedEvent.getDuration());


            int maxShowDur = Math.min(assignedEvent.getDuration(), 180);
            int showDuration = 10 + random.nextInt(maxShowDur - 9);


            long eventTotalMinutes = assignedEvent.getDuration();
            long latestOffset = eventTotalMinutes - showDuration;

            long offsetMinutes = (latestOffset > 0)
                ? random.nextInt((int) latestOffset + 1)
                : 0;
            LocalDateTime showStart = eventStart.plusMinutes(offsetMinutes)
                .truncatedTo(ChronoUnit.MINUTES);


            Room assignedRoom = rooms.get(random.nextInt(rooms.size()));

            Show show = Show.ShowBuilder.aShow()
                .withName("Show " + j)
                .withDuration(showDuration)
                .withDate(showStart)
                .withEvent(assignedEvent)
                .withRoom(assignedRoom)
                .build();

            shows.add(show);
        }

        showRepository.saveAll(shows);
        LOGGER.debug("Saved {} Shows", shows.size());

        List<Show> persistedShows = showRepository.findAll();
        if (persistedShows.isEmpty()) {
            LOGGER.warn("No Shows found after saving: cannot assign Artists");
            return;
        }

        List<Artist> artists = new ArrayList<>(NUMBER_OF_ARTISTS);
        LOGGER.debug("Generating {} Artists", NUMBER_OF_ARTISTS);
        for (int k = 0; k < NUMBER_OF_ARTISTS; k++) {

            String firstName = "ArtistFirst" + k;
            String lastName = "ArtistLast" + k;
            String stageName = "Stagename" + k;

            int showsCount = MIN_SHOWS_PER_ARTIST + random.nextInt(MAX_SHOWS_PER_ARTIST - MIN_SHOWS_PER_ARTIST + 1);

            Set<Show> assignedShows = new HashSet<>();
            for (int s = 0; s < showsCount; s++) {
                Show randomShow = persistedShows.get(random.nextInt(persistedShows.size()));
                assignedShows.add(randomShow);
            }

            Artist artist = Artist.ArtistBuilder.anArtist()
                .withFirstname(firstName)
                .withLastname(lastName)
                .withStagename(stageName)
                .withShows(assignedShows)
                .build();

            artists.add(artist);
        }

        artistRepository.saveAll(artists);
        LOGGER.debug("Saved {} Artists", artists.size());
    }
}
