package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event.EventCategory;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.CustomSearchService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.SearchValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomSearchServiceTest {
    @Mock
    private EventRepository eventRepo;
    @Mock
    private SearchValidator validator;
    @InjectMocks
    private CustomSearchService service;

    private Event event;

    @BeforeEach
    void setUp() throws Exception {
        event = new Event();
        event.setId(1L);
        event.setName("Test");
        event.setCategory(EventCategory.ROCK);
        EventLocation loc = new EventLocation(); loc.setId(2L);
        event.setLocation(loc);
        event.setDescription("Description");
        Field f = Event.class.getDeclaredField("totalDuration");
        f.setAccessible(true);
        f.set(event, 90);
    }

    @Test
    void searchEvents_MapsEntitiesToDto() {
        Page<Event> stubPage = new PageImpl<>(List.of(event), PageRequest.of(0,10), 1);

        when(eventRepo.findAll(
            ArgumentMatchers.<Specification<Event>>any(),
            any(Pageable.class)
        )).thenReturn(stubPage);

        doNothing().when(validator).validateForEvents(any(EventSearchDto.class));

        EventSearchDto dto = new EventSearchDto();
        dto.setPage(0);
        dto.setSize(10);

        Page<EventSearchResultDto> result = service.searchEvents(dto);

        assertEquals(1, result.getTotalElements());
        EventSearchResultDto out = result.getContent().getFirst();
        assertEquals(1L, out.getId());
        assertEquals("Test", out.getName());
        assertEquals("Rock", out.getCategory());
        assertEquals(2L, out.getLocationId());
        assertEquals(90, out.getDuration());
        assertEquals("Description", out.getDescription());

        verify(validator).validateForEvents(dto);
    }
}
