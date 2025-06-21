package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageService {

    /**
     * Find all message entries ordered by published at date (descending).
     *
     * @return ordered list of al message entries
     */
    List<Message> findAll();

    /**
     * Find all message entries paginated.
     *
     * @param pageable the pagination information
     * @return paginated list of all message entries
     */
    Page<Message> findAllPaginated(Pageable pageable);

    /**
     * Find a single message entry by id.
     *
     * @param id the id of the message entry
     * @return the message entry
     */
    Message findOne(Long id);

    /**
     * Publish a single message entry.
     *
     * @param message to publish
     * @return published message entry
     */
    Message publishMessage(Message message);

    /**
     * Find a message with image.
     *
     * @param id of News
     * @return message entity
     */
    Message findOneWithImage(Long id);
}
