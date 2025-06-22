package at.ac.tuwien.sepr.groupphase.backend.unittests.Service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Image;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ImageRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ImageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ImageServiceImplTest {

    private ImageRepository imageRepository;
    private ImageServiceImpl imageService;

    @BeforeEach
    void setUp() {
        imageRepository = mock(ImageRepository.class);
        imageService = new ImageServiceImpl(imageRepository);
    }

    @Test
    void testFindById_ExistingId() {
        Image image = new Image();
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        Image result = imageService.findById(1L);

        assertEquals(image, result);
        verify(imageRepository).findById(1L);
    }

    @Test
    void testFindById_NonExistingId() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> imageService.findById(1L));
        verify(imageRepository).findById(1L);
    }
}