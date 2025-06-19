package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ImageRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.MessageRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.MessageService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class SimpleMessageService implements MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final MessageRepository messageRepository;
    private final ImageRepository imageRepository;

    public SimpleMessageService(MessageRepository messageRepository, ImageRepository imageRepository) {
        this.messageRepository = messageRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    public List<Message> findAll() {
        LOGGER.debug("Find all messages");
        return messageRepository.findAllByOrderByPublishedAtDesc();
    }

    @Override
    public Message findOne(Long id) {
        LOGGER.debug("Find message with id {}", id);
        Optional<Message> message = messageRepository.findById(id);
        if (message.isPresent()) {
            return message.get();
        } else {
            throw new NotFoundException(String.format("Could not find message with id %s", id));
        }
    }

    @Override
    public Message publishMessage(Message message) {
        LOGGER.debug("Publish new message {}", message);
        message.setPublishedAt(LocalDateTime.now());
        if (message.getImages() != null && !message.getImages().isEmpty()) {
            imageRepository.saveAll(message.getImages());
        }
        return messageRepository.save(message);
    }


    @Override
    public Message findOneWithImage(Long id) {
        LOGGER.debug("Find message with id {}", id);
        Optional<Message> message = messageRepository.findById(id);
        if (message.isPresent()) {
            var messageWithImage = message.get();
            Hibernate.initialize(messageWithImage.getImages());
            return messageWithImage;
        } else {
            throw new NotFoundException(String.format("Could not find message with id %s", id));
        }

    }

}
