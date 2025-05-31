package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component("eventDataGenerator")
@DependsOn("roomDataGenerator")
@Profile("generateData")
public class EventDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventDataGenerator.class);

    private static final int NUMBER_OF_EVENTS = 5;
    private static final int NUMBER_OF_ARTISTS = 20;
    private static final int SHOWS_PER_ARTIST_PAIR = 2;

    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;
    private final ShowRepository showRepository;
    private final RoomRepository roomRepository;

    public EventDataGenerator(EventRepository eventRepository,
                              ArtistRepository artistRepository,
                              ShowRepository showRepository,
                              RoomRepository roomRepository) {
        this.eventRepository = eventRepository;
        this.artistRepository = artistRepository;
        this.showRepository = showRepository;
        this.roomRepository = roomRepository;
    }

    @PostConstruct
    public void generateEvents() {
        if (eventRepository.count() == 0) {
            var rooms = roomRepository.findAll();
            List<Event> events = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_EVENTS; i++) {
                LocalDateTime eventStart = LocalDateTime.of(2025, 8, i + 1, 14, 0);
                int duration = 180 + i * 10;

                Event ev = Event.EventBuilder.anEvent()
                    .withName("Event " + i)
                    .withCategory(Event.EventCategory.CLASSICAL)
                    .withDescription("Description for Event " + i)
                    .withDateTime(eventStart)
                    .withDuration(duration)
                    .withLocation(rooms.get(i % rooms.size()).getEventLocation())
                    .build();
                events.add(ev);
            }
            eventRepository.saveAll(events);
        }

        if (artistRepository.count() == 0) {
            List<Artist> artists = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_ARTISTS; i++) {
                Artist artist = Artist.ArtistBuilder.anArtist()
                    .withFirstname("Firstname " + i)
                    .withLastname("Lastname " + i)
                    .withStagename("Stagename " + i)
                    .build();
                artists.add(artist);
            }
            artistRepository.saveAll(artists);
        }

        if (showRepository.count() == 0) {
            var events = eventRepository.findAll();
            var artists = artistRepository.findAll();
            var rooms = roomRepository.findAll();

            if (events.isEmpty() || artists.size() < 2 || rooms.isEmpty()) {
                return;
            }

            List<Show> shows = new ArrayList<>();
            int artistIndex = 0;
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                LocalDateTime eventStart = event.getDateTime();
                int eventDuration = event.getDuration();
                int showDuration = eventDuration / SHOWS_PER_ARTIST_PAIR;

                for (int j = 0; j < SHOWS_PER_ARTIST_PAIR; j++) {
                    LocalDateTime showStart = eventStart.plusMinutes((long) j * showDuration);
                    LocalDateTime showEnd = showStart.plusMinutes(showDuration);
                    LocalDateTime eventEnd = eventStart.plusMinutes(eventDuration);
                    if (showEnd.isAfter(eventEnd)) {
                        showStart = eventEnd.minusMinutes(showDuration);
                    }

                    var a1 = artists.get(artistIndex % artists.size());
                    var a2 = artists.get((artistIndex + 1) % artists.size());

                    Show show = Show.ShowBuilder.aShow()
                        .withName(event.getName() + " - Show " + j)
                        .withDuration(showDuration)
                        .withDate(showStart)
                        .withEvent(event)
                        .withRoom(rooms.get(i % rooms.size()))
                        .withArtists(Set.of(a1, a2))
                        .build();
                    shows.add(show);
                    artistIndex += 2;
                }
            }
            showRepository.saveAll(shows);
        }
    }
}
