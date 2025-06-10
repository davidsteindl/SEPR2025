package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.StageSector;
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

import java.util.ArrayList;
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
            return;
        }

        List<EventLocation> locations = locationRepository.findAll();
        if (locations.isEmpty()) {
            return;
        }

        List<Room> rooms = new ArrayList<>();

        for (EventLocation loc : locations) {
            for (int r = 0; r < ROOMS_PER_LOCATION; r++) {
                Room room = Room.RoomBuilder.aRoom()
                    .withName(loc.getName() + " - Room " + (r + 1))
                    .withEventLocation(loc)
                    .build();

                List<Seat> allSeats = new ArrayList<>();

                for (int row = 1; row <= SEATED_ROWS; row++) {
                    for (int col = 1; col <= SEATED_COLUMNS; col++) {
                        Seat seat = new Seat();
                        seat.setRowNumber(row);
                        seat.setColumnNumber(col);
                        seat.setDeleted(false);
                        seat.setRoom(room);
                        allSeats.add(seat);
                    }
                }

                Sector sector = new Sector();
                sector.setRoom(room);
                sector.setPrice(SEATED_PRICE);

                for (Seat seat : allSeats) {
                    if (seat.getColumnNumber() <= SEATED_COLUMNS / 2 && seat.getRowNumber() > 1) {
                        seat.setSector(sector);
                    }
                }

                StandingSector standingSector = new StandingSector();
                standingSector.setRoom(room);
                standingSector.setPrice(STANDING_PRICE);
                standingSector.setCapacity(STANDING_CAPACITY);

                for (Seat seat : allSeats) {
                    if (seat.getColumnNumber() > SEATED_COLUMNS / 2 && seat.getRowNumber() > 1) {
                        seat.setSector(standingSector);
                    }
                }

                StageSector stageSector = new StageSector();
                stageSector.setRoom(room);

                for (Seat seat : allSeats) {
                    if (seat.getRowNumber() == 1) {
                        seat.setSector(stageSector);
                    }
                }

                room.setSeats(allSeats);
                room.addSector(sector);
                room.addSector(standingSector);
                room.addSector(stageSector);

                rooms.add(room);
            }
        }

        roomRepository.saveAll(rooms);
    }
}
