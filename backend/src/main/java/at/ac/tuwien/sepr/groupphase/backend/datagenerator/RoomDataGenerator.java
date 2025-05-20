package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.SeatedSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;

@Component("roomDataGenerator")
@DependsOn("locationSeeder")
@Profile("generateData")
public class RoomDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoomDataGenerator.class);
    private static final int ROOMS_PER_LOCATION = 2;
    private static final int SEATED_ROWS = 5;
    private static final int SEATED_COLUMNS = 10;
    private static final int SEATED_PRICE = 50;
    private static final int STANDING_CAPACITY = 100;
    private static final int STANDING_PRICE = 30;

    private final EventLocationRepository locationRepository;
    private final RoomRepository roomRepository;

    public RoomDataGenerator(EventLocationRepository locationRepository,
                             RoomRepository roomRepository) {
        this.locationRepository = locationRepository;
        this.roomRepository = roomRepository;
    }

    @PostConstruct
    public void generateRooms() {
        if (roomRepository.count() > 0) {
            LOGGER.debug("Rooms already generated, skipping RoomDataGenerator");
            return;
        }
        List<EventLocation> locations = locationRepository.findAll();
        if (locations.isEmpty()) {
            LOGGER.warn("No EventLocations found: generate EventLocations first");
            return;
        }

        List<Room> rooms = new java.util.ArrayList<>();
        LOGGER.debug("Generating rooms for {} locations", locations.size());
        for (EventLocation loc : locations) {
            for (int r = 0; r < ROOMS_PER_LOCATION; r++) {
                Room room = Room.RoomBuilder.aRoom()
                    .name(loc.getName() + " - Room " + (r + 1))
                    .eventLocation(loc)
                    .build();

                SeatedSector seated = SeatedSector.SeatedSectorBuilder
                    .aSeatedSector()
                    .price(SEATED_PRICE)
                    .room(room)
                    .build();
                for (int row = 1; row <= SEATED_ROWS; row++) {
                    for (int col = 1; col <= SEATED_COLUMNS; col++) {
                        Seat seat = new Seat();
                        seat.setRowNumber(row);
                        seat.setColumnNumber(col);
                        seat.setDeleted(false);
                        seated.addSeat(seat);
                    }
                }
                room.addSector(seated);

                StandingSector standing = StandingSector.StandingSectorBuilder
                    .aStandingSector()
                    .price(STANDING_PRICE)
                    .capacity(STANDING_CAPACITY)
                    .room(room)
                    .build();
                room.addSector(standing);

                rooms.add(room);
            }
        }
        roomRepository.saveAll(rooms);
        LOGGER.debug("Saved {} rooms (with sectors & seats)", rooms.size());
    }
}
