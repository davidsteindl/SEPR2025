package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.service.specifications.ArtistSpecifications;
import at.ac.tuwien.sepr.groupphase.backend.service.specifications.EventLocationSpecifications;
import at.ac.tuwien.sepr.groupphase.backend.service.specifications.EventSpecifications;
import at.ac.tuwien.sepr.groupphase.backend.service.specifications.ShowSpecifications;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SearchSpecificationsTest {

    private CriteriaBuilder cb;
    private Root root;
    private CriteriaQuery query;
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        cb = mock(CriteriaBuilder.class);
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        predicate = mock(Predicate.class);

        when(cb.conjunction()).thenReturn(predicate);
        when(cb.like(any(), anyString())).thenReturn(predicate);

        when(cb.between(any(), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(predicate);
        when(cb.between(any(), any(Integer.class), any(Integer.class)))
            .thenReturn(predicate);
        when(cb.between(any(), any(BigDecimal.class), any(BigDecimal.class)))
            .thenReturn(predicate);

        when(cb.greaterThanOrEqualTo(any(), any(LocalDateTime.class)))
            .thenReturn(predicate);
        when(cb.lessThanOrEqualTo(any(), any(LocalDateTime.class)))
            .thenReturn(predicate);

        when(cb.greaterThanOrEqualTo(any(), any(BigDecimal.class)))
            .thenReturn(predicate);
        when(cb.lessThanOrEqualTo(any(), any(BigDecimal.class)))
            .thenReturn(predicate);

        Path mockPath = mock(Path.class);
        when(root.get(anyString())).thenReturn(mockPath);
        when(mockPath.get(anyString())).thenReturn(mockPath);
        when(mockPath.as(any())).thenReturn(mockPath);
    }


    @Test
    void testArtistSpecifications_All() {
        ArtistSpecifications.hasFirstnameLike("John").toPredicate(root, query, cb);
        ArtistSpecifications.hasFirstnameLike(null).toPredicate(root, query, cb);
        ArtistSpecifications.hasFirstnameLike("").toPredicate(root, query, cb);

        ArtistSpecifications.hasLastnameLike("Doe").toPredicate(root, query, cb);
        ArtistSpecifications.hasLastnameLike(null).toPredicate(root, query, cb);

        ArtistSpecifications.hasStagenameLike("Stage").toPredicate(root, query, cb);
        ArtistSpecifications.hasStagenameLike("").toPredicate(root, query, cb);
    }

    @Test
    void testEventLocationSpecifications_All() {
        EventLocationSpecifications.hasNameLike("Gasometer").toPredicate(root, query, cb);
        EventLocationSpecifications.hasNameLike(null).toPredicate(root, query, cb);

        EventLocationSpecifications.hasStreetLike("Street").toPredicate(root, query, cb);
        EventLocationSpecifications.hasStreetLike(null).toPredicate(root, query, cb);

        EventLocationSpecifications.hasCityLike("Vienna").toPredicate(root, query, cb);
        EventLocationSpecifications.hasCityLike("").toPredicate(root, query, cb);

        EventLocationSpecifications.hasCountryLike("Austria").toPredicate(root, query, cb);
        EventLocationSpecifications.hasCountryLike(null).toPredicate(root, query, cb);

        EventLocationSpecifications.hasPostalCodeLike("1234").toPredicate(root, query, cb);
        EventLocationSpecifications.hasPostalCodeLike("").toPredicate(root, query, cb);
    }

    @Test
    void testEventSpecifications_All() {
        Path mockPath = mock(Path.class);
        when(root.get(anyString())).thenReturn(mockPath);
        when(mockPath.as(any())).thenReturn(mockPath);

        EventSpecifications.hasName("Rock").toPredicate(root, query, cb);
        EventSpecifications.hasName("").toPredicate(root, query, cb);

        EventSpecifications.hasCategory("Rock").toPredicate(root, query, cb);
        EventSpecifications.hasCategory(null).toPredicate(root, query, cb);

        EventSpecifications.hasDescription("Concert").toPredicate(root, query, cb);
        EventSpecifications.hasDescription("").toPredicate(root, query, cb);

        EventSpecifications.hasDurationBetween(90).toPredicate(root, query, cb);
    }

    @Test
    void testShowSpecifications_All() {
        Path mockPath = mock(Path.class);
        Join joinRoom = mock(Join.class);
        Join joinSector = mock(Join.class);

        when(root.get("event")).thenReturn(mockPath);
        when(root.get("room")).thenReturn(mockPath);
        when(root.join("room")).thenReturn(joinRoom);
        when(joinRoom.join("sectors")).thenReturn(joinSector);

        when(mockPath.get(anyString())).thenReturn(mockPath);
        when(joinRoom.get(anyString())).thenReturn(mockPath);
        when(joinSector.get(anyString())).thenReturn(mockPath);

        ShowSpecifications.dateBetween(LocalDateTime.now(), LocalDateTime.now().plusDays(1))
            .toPredicate(root, query, cb);
        ShowSpecifications.dateBetween(LocalDateTime.now(), null)
            .toPredicate(root, query, cb);
        ShowSpecifications.dateBetween(null, LocalDateTime.now()).toPredicate(root, query, cb);
        ShowSpecifications.dateBetween(null, null).toPredicate(root, query, cb);

        ShowSpecifications.hasEventName("Rock").toPredicate(root, query, cb);
        ShowSpecifications.hasEventName("").toPredicate(root, query, cb);

        ShowSpecifications.hasRoomName("Main").toPredicate(root, query, cb);
        ShowSpecifications.hasRoomName(null).toPredicate(root, query, cb);

        ShowSpecifications.nameContains("Show").toPredicate(root, query, cb);
        ShowSpecifications.nameContains("").toPredicate(root, query, cb);

        ShowSpecifications.hasPriceBetween(BigDecimal.ONE, BigDecimal.TEN)
            .toPredicate(root, query, cb);
        ShowSpecifications.hasPriceBetween(BigDecimal.ONE, null)
            .toPredicate(root, query, cb);
        ShowSpecifications.hasPriceBetween(null, BigDecimal.TEN)
            .toPredicate(root, query, cb);
        ShowSpecifications.hasPriceBetween(null, null)
            .toPredicate(root, query, cb);
    }
}
