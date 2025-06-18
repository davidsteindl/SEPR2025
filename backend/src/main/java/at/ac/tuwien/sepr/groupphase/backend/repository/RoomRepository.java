package at.ac.tuwien.sepr.groupphase.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    /**
     * Finds all rooms with their associated sectors and seats eagerly loaded.
     *
     * @return a list of all rooms with sectors
     */
    @Query("SELECT DISTINCT r FROM Room r "
        + "LEFT JOIN FETCH r.sectors "
        + "LEFT JOIN FETCH r.seats")
    List<Room> findAllWithSectorsAndSeats();

    @EntityGraph(attributePaths = {"sectors", "seats"})
    @Override
    Page<Room> findAll(Pageable pageable);
}
