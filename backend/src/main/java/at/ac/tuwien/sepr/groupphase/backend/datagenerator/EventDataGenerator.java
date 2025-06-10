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
    private static final int NUMBER_OF_FUTURE_EVENTS = 20;
    private static final int NUMBER_OF_PAST_EVENTS = 5;
    private static final int NUMBER_OF_FUTURE_SHOWS = 50;
    private static final int NUMBER_OF_PAST_SHOWS = 5;
    private static final int NUMBER_OF_ARTISTS = 10;
    private static final int MIN_SHOWS_PER_ARTIST = 1;
    private static final int MAX_SHOWS_PER_ARTIST = 2;

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
        LOGGER.debug("Generating test data for Events, Shows, and Artists...");

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
        List<Event> pastEvents = generatePastEvents(locations);

        List<Event> futureEvents = generateFutureEvents(locations);

        List<Show> allShows = generateAllShows(pastEvents, futureEvents, rooms);

        generateArtists(allShows);

        LOGGER.debug("Generated {} Past Events, {} Future Events, {} Shows, and {} Artists",
            pastEvents.size(), futureEvents.size(), allShows.size(), NUMBER_OF_ARTISTS);
    }

    private List<Event> generatePastEvents(List<EventLocation> locations) {
        List<Event> pastEvents = new ArrayList<>(NUMBER_OF_PAST_EVENTS);
        Event.EventCategory[] categories = Event.EventCategory.values();

        LOGGER.debug("Starting generatePastEvents(): Generating {} Past Events", NUMBER_OF_PAST_EVENTS);
        for (int i = 0; i < NUMBER_OF_PAST_EVENTS; i++) {
            EventLocation loc = locations.get(random.nextInt(locations.size()));

            LocalDateTime now = LocalDateTime.now();
            long daysBack = 1 + random.nextInt(365);
            LocalDateTime eventStart = now
                .minusDays(daysBack)
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60))
                .truncatedTo(ChronoUnit.MINUTES);

            int eventDuration = 180 + random.nextInt(1261);
            Event.EventCategory category = categories[random.nextInt(categories.length)];

            Event event = Event.EventBuilder.anEvent()
                .withName("PastEvent " + i)
                .withCategory(category)
                .withDescription("Description of PastEvent " + i)
                .withDateTime(eventStart)
                .withDuration(eventDuration)
                .withLocation(loc)
                .build();

            pastEvents.add(event);
        }
        eventRepository.saveAll(pastEvents);
        LOGGER.debug("generatePastEvents(): Saved {} Past Events", pastEvents.size());
        return pastEvents;
    }

    private List<Event> generateFutureEvents(List<EventLocation> locations) {
        List<Event> futureEvents = new ArrayList<>(NUMBER_OF_FUTURE_EVENTS);
        Event.EventCategory[] categories = Event.EventCategory.values();

        LOGGER.debug("generateFutureEvents(): Generating {} Future Events", NUMBER_OF_FUTURE_EVENTS);
        for (int i = 0; i < NUMBER_OF_FUTURE_EVENTS; i++) {
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

            futureEvents.add(event);
        }
        eventRepository.saveAll(futureEvents);
        LOGGER.debug("generateFutureEvents(): Saved {} Future Events", futureEvents.size());
        return futureEvents;
    }

    private List<Show> generateAllShows(List<Event> pastEvents, List<Event> futureEvents, List<Room> rooms) {
        List<Show> shows = new ArrayList<>(NUMBER_OF_FUTURE_SHOWS + NUMBER_OF_PAST_SHOWS);
        LOGGER.debug("Starting generateAllShows(): Generating future and past shows");

        LOGGER.debug("Generating {} Future Shows", NUMBER_OF_FUTURE_SHOWS);
        for (int j = 0; j < NUMBER_OF_FUTURE_SHOWS; j++) {
            Event assignedEvent = futureEvents.get(random.nextInt(futureEvents.size()));
            LocalDateTime eventStart = assignedEvent.getDateTime();

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

            Show futureShow = Show.ShowBuilder.aShow()
                .withName("Show " + j)
                .withDuration(showDuration)
                .withDate(showStart)
                .withEvent(assignedEvent)
                .withRoom(assignedRoom)
                .build();

            shows.add(futureShow);
        }

        LOGGER.debug("Generating {} Past Shows", NUMBER_OF_PAST_SHOWS);
        for (int k = 0; k < NUMBER_OF_PAST_SHOWS; k++) {
            Event assignedEvent = pastEvents.get(random.nextInt(pastEvents.size()));
            LocalDateTime eventStart = assignedEvent.getDateTime();

            LocalDateTime eventEnd = eventStart.plusMinutes(assignedEvent.getDuration());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime latestPossibleStart = eventEnd.isBefore(now) ? eventEnd : now.minusMinutes(10);

            long totalPastWindow = ChronoUnit.MINUTES.between(eventStart, latestPossibleStart);
            long offsetPast = (totalPastWindow > 0) ? random.nextInt((int) totalPastWindow + 1) : 0;
            LocalDateTime showStart = eventStart.plusMinutes(offsetPast)
                .truncatedTo(ChronoUnit.MINUTES);

            int showDuration = 10 + random.nextInt(171);
            Room assignedRoom = rooms.get(random.nextInt(rooms.size()));

            Show pastShow = Show.ShowBuilder.aShow()
                .withName("PastShow " + k)
                .withDuration(showDuration)
                .withDate(showStart)
                .withEvent(assignedEvent)
                .withRoom(assignedRoom)
                .build();

            shows.add(pastShow);
        }
        showRepository.saveAll(shows);
        LOGGER.debug("generateAllShows(): Saved {} Shows ({} future + {} past)", shows.size(), NUMBER_OF_FUTURE_SHOWS, NUMBER_OF_PAST_SHOWS);

        return showRepository.findAll();
    }

    private void generateArtists(List<Show> persistedShows) {
        if (persistedShows.isEmpty()) {
            LOGGER.warn("No Shows found after saving: cannot assign Artists");
            return;
        }

        List<Artist> artists = new ArrayList<>(NUMBER_OF_ARTISTS);
        LOGGER.debug("Starting generateArtists(): Generating {} Artists", NUMBER_OF_ARTISTS);
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
        LOGGER.debug("generateArtists(): Saved {} Artists", artists.size());
    }
}
