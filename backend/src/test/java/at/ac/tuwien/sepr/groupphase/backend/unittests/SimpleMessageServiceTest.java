package at.ac.tuwien.sepr.groupphase.backend.unittests;


import at.ac.tuwien.sepr.groupphase.backend.entity.Image;
import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ImageRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.MessageRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class SimpleMessageServiceTest {

    private ImageRepository imageRepository;
    private MessageRepository messageRepository;
    private SimpleMessageService simpleMessageService;


    @BeforeEach
    void setUp() {
        imageRepository = mock(ImageRepository.class);
        messageRepository = mock(MessageRepository.class);

        simpleMessageService = new SimpleMessageService(messageRepository, imageRepository);

    }

    @Test
    void testFindAll() {
        Message message = new Message();
        when(messageRepository.findAllByOrderByPublishedAtDesc()).thenReturn(Collections.singletonList(message));

        List<Message> messages = simpleMessageService.findAll();

        assertEquals(1, messages.size());
        verify(messageRepository).findAllByOrderByPublishedAtDesc();
    }

    @Test
    void testFindOne_ExistingId() {
        Message message = new Message();
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        Message result = simpleMessageService.findOne(1L);

        assertEquals(message, result);
        verify(messageRepository).findById(1L);
    }

    @Test
    void testFindOne_NonExistingId() {
        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> simpleMessageService.findOne(1L));
        verify(messageRepository).findById(1L);
    }

    @Test
    void testPublishMessage_WithImages() {
        Message message = new Message();
        message.setImages(Collections.singletonList(new Image()));

        simpleMessageService.publishMessage(message);

        assertNotNull(message.getPublishedAt());
        verify(imageRepository).saveAll(message.getImages());
        verify(messageRepository).save(message);
    }

    @Test
    void testPublishMessage_WithoutImages() {
        Message message = new Message();

        simpleMessageService.publishMessage(message);

        assertNotNull(message.getPublishedAt());
        verify(imageRepository, never()).saveAll(any());
        verify(messageRepository).save(message);
    }



}
