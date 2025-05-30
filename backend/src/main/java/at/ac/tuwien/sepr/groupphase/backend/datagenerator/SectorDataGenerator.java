package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SectorRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
@Profile("generateData")
public class SectorDataGenerator {

    private final SectorRepository sectorRepository;
    private final RoomRepository roomRepository;

    public SectorDataGenerator(SectorRepository sectorRepository, RoomRepository roomRepository) {
        this.sectorRepository = sectorRepository;
        this.roomRepository = roomRepository;
    }

    @PostConstruct
    public void generateSectors() {
        if (sectorRepository.count() > 0) {
            return;
        }

        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) {
            throw new IllegalStateException("No rooms found");
        }

        Room room = rooms.getFirst();

        for (int i = 0; i < 3; i++) {
            SeatedSector sector = new SeatedSector();
            sector.setPrice(1500 + i * 100);
            sector.setRoom(room);
            sectorRepository.save(sector);
        }
    }
}

