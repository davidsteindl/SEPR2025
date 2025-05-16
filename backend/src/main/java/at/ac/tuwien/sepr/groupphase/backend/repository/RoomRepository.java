package at.ac.tuwien.sepr.groupphase.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

}
