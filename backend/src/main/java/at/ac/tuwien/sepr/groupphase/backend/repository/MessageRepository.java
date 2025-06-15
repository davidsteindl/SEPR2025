package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all message entries ordered by published at date (descending).
     *
     * @return ordered list of al message entries
     */
    List<Message> findAllByOrderByPublishedAtDesc();

    /**
     * Find all messages that a given user has NOT viewed yet, ordered by published at date (descending).
     *
     * @param userId the id of the ApplicationUser
     * @return list of unseen messages
     */
    @Query("""
            SELECT m FROM Message m
            WHERE :userId NOT IN (
                SELECT u.id FROM m.viewers u
            )
            ORDER BY m.publishedAt DESC
        """)
    List<Message> findAllUnseenByUserIdOrderByPublishedAtDesc(@Param("userId") Long userId);
}
