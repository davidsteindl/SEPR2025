package at.ac.tuwien.sepr.groupphase.backend.unittests;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.SearchValidator;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SearchValidatorTest {
    private SearchValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SearchValidator();
    }

    @Test
    void validateForArtists_NullDto_Throws() {
        assertThrows(ValidationException.class, () -> validator.validateForArtists(null));
    }

    @Test
    void validateForArtists_AllFieldsEmpty_Throws() {
        ArtistSearchDto dto = new ArtistSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        assertThrows(ValidationException.class, () -> validator.validateForArtists(dto));
    }

    @Test
    void validateForArtists_InvalidPage_Throws() {
        ArtistSearchDto dto = new ArtistSearchDto();
        dto.setPage(-1);
        dto.setSize(10);
        dto.setFirstname("A");
        assertThrows(ValidationException.class, () -> validator.validateForArtists(dto));
    }

    @Test
    void validateForArtists_InvalidSize_Throws() {
        ArtistSearchDto dto = new ArtistSearchDto();
        dto.setPage(0);
        dto.setSize(0);
        dto.setFirstname("A");
        assertThrows(ValidationException.class, () -> validator.validateForArtists(dto));
    }

    @Test
    void validateForArtists_FirstnameTooLong_Throws() {
        ArtistSearchDto dto = new ArtistSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setFirstname("A".repeat(101));
        assertThrows(ValidationException.class, () -> validator.validateForArtists(dto));
    }

    @Test
    void validateForArtists_LastnameTooLong_Throws() {
        ArtistSearchDto dto = new ArtistSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setLastname("B".repeat(101));
        assertThrows(ValidationException.class, () -> validator.validateForArtists(dto));
    }

    @Test
    void validateForArtists_StagenameTooLong_Throws() {
        ArtistSearchDto dto = new ArtistSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setStagename("C".repeat(101));
        assertThrows(ValidationException.class, () -> validator.validateForArtists(dto));
    }

    @Test
    void validateForArtists_ValidFirstname_DoesNotThrow() {
        ArtistSearchDto dto = new ArtistSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setFirstname("Freddie");
        assertDoesNotThrow(() -> validator.validateForArtists(dto));
    }

    @Test
    void validateForArtists_ValidLastname_DoesNotThrow() {
        ArtistSearchDto dto = new ArtistSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setLastname("Mercury");
        assertDoesNotThrow(() -> validator.validateForArtists(dto));
    }

    @Test
    void validateForArtists_ValidStagename_DoesNotThrow() {
        ArtistSearchDto dto = new ArtistSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setStagename("Queen");
        assertDoesNotThrow(() -> validator.validateForArtists(dto));
    }

    @Test
    void validateForEventLocations_NullDto_Throws() {
        assertThrows(ValidationException.class, () -> validator.validateForEventLocations(null));
    }

    @Test
    void validateForEventLocations_AllFieldsEmpty_Throws() {
        EventLocationSearchDto dto = new EventLocationSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        assertThrows(ValidationException.class, () -> validator.validateForEventLocations(dto));
    }

    @Test
    void validateForEventLocations_InvalidPage_Throws() {
        EventLocationSearchDto dto = new EventLocationSearchDto();
        dto.setPage(-1);
        assertThrows(ValidationException.class, () -> validator.validateForEventLocations(dto));
    }

    @Test
    void validateForEventLocations_InvalidSize_Throws() {
        EventLocationSearchDto dto = new EventLocationSearchDto();
        dto.setPage(0);
        dto.setSize(0);
        dto.setCity("Y");
        assertThrows(ValidationException.class,
            () -> validator.validateForEventLocations(dto));
    }

    @Test
    void validateForEventLocations_InvalidNameLength_Throws() {
        EventLocationSearchDto dto = new EventLocationSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setName("A".repeat(101));
        assertThrows(ValidationException.class,
            () -> validator.validateForEventLocations(dto));
    }

    @Test
    void validateForEventLocations_InvalidStreetLength_Throws() {
        EventLocationSearchDto dto = new EventLocationSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setStreet("B".repeat(101));
        assertThrows(ValidationException.class,
            () -> validator.validateForEventLocations(dto));
    }

    @Test
    void validateForEventLocations_InvalidCityLength_Throws() {
        EventLocationSearchDto dto = new EventLocationSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setCity("C".repeat(101));
        assertThrows(ValidationException.class,
            () -> validator.validateForEventLocations(dto));
    }

    @Test
    void validateForEventLocations_InvalidPostalCodeLength_Throws() {
        EventLocationSearchDto dto = new EventLocationSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setPostalCode("1".repeat(101));
        assertThrows(ValidationException.class,
            () -> validator.validateForEventLocations(dto));
    }

    @Test
    void validateForEventLocations_ValidDto_DoesNotThrow() {
        EventLocationSearchDto dto = new EventLocationSearchDto();
        dto.setPage(0);
        dto.setSize(5);
        dto.setName("Gasometer");
        dto.setStreet("Guglgasse 6");
        dto.setCity("Vienna");
        dto.setCountry("Austria");
        dto.setPostalCode("1110");
        assertDoesNotThrow(() -> validator.validateForEventLocations(dto));
    }

    @Test
    void validateForEvent_NullDto_Throws() {
        assertThrows(ValidationException.class, () -> validator.validateForEvents(null));
    }

    @Test
    void validateForEvents_AllFieldsEmpty_Throws() {
        EventSearchDto dto = new EventSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        assertThrows(ValidationException.class, () -> validator.validateForEvents(dto));
    }

    @Test
    void validateForEvents_InvalidPage_Throws() {
        EventSearchDto dto = new EventSearchDto();
        dto.setPage(-1);
        assertThrows(ValidationException.class, () -> validator.validateForEvents(dto));
    }

    @Test
    void validateForEvents_InvalidSize_Throws() {
        EventSearchDto dto = new EventSearchDto();
        dto.setSize(0);
        assertThrows(ValidationException.class, () -> validator.validateForEvents(dto));
    }

    @Test
    void validateForEvents_InvalidNameLength_Throws() {
        EventSearchDto dto = new EventSearchDto();
        dto.setName("A".repeat(101));
        assertThrows(ValidationException.class, () -> validator.validateForEvents(dto));
    }

    @Test
    void validateForEvents_InvalidType_Throws() {
        EventSearchDto dto = new EventSearchDto();
        dto.setCategory("UnknownType");
        assertThrows(ValidationException.class, () -> validator.validateForEvents(dto));
    }

    @Test
    void validateForEvents_InvalidDescriptionLength_Throws() {
        EventSearchDto dto = new EventSearchDto();
        dto.setDescription("A".repeat(501));
        assertThrows(ValidationException.class, () -> validator.validateForEvents(dto));
    }

    @Test
    void validateForEvents_InvalidDuration_Throws() {
        EventSearchDto dto = new EventSearchDto();
        dto.setDuration(-10);
        assertThrows(ValidationException.class, () -> validator.validateForEvents(dto));
    }

    @Test
    void validateForEvents_ValidDto_DoesNotThrow() {
        EventSearchDto dto = new EventSearchDto();
        dto.setPage(0);
        dto.setSize(5);
        dto.setName("Concert");
        dto.setCategory("Rock");
        dto.setDescription("A show");
        dto.setDuration(120);
        assertDoesNotThrow(() -> validator.validateForEvents(dto));
    }

    @Test
    void validateForShows_NullDto_Throws() {
        assertThrows(ValidationException.class, () -> validator.validateForShows(null));
    }

    @Test
    void validateForShows_AllFieldsEmpty_Throws() {
        ShowSearchDto dto = new ShowSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        assertThrows(ValidationException.class, () -> validator.validateForShows(dto));
    }

    @Test
    void validateForShows_InvalidPage_Throws() {
        ShowSearchDto dto = new ShowSearchDto();
        dto.setPage(-1);
        dto.setSize(10);
        dto.setName("A");
        assertThrows(ValidationException.class, () -> validator.validateForShows(dto));
    }

    @Test
    void validateForShows_InvalidSize_Throws() {
        ShowSearchDto dto = new ShowSearchDto();
        dto.setPage(0);
        dto.setSize(0);
        dto.setName("A");
        assertThrows(ValidationException.class, () -> validator.validateForShows(dto));
    }

    @Test
    void validateForShows_EndBeforeStart_Throws() {
        ShowSearchDto dto = new ShowSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setStartDate(LocalDateTime.now());
        dto.setEndDate(LocalDateTime.now().minusDays(1));
        assertThrows(ValidationException.class, () -> validator.validateForShows(dto));
    }

    @Test
    void validateForShows_MinPriceNegative_Throws() {
        ShowSearchDto dto = new ShowSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setMinPrice(new BigDecimal("-1"));
        assertThrows(ValidationException.class, () -> validator.validateForShows(dto));
    }

    @Test
    void validateForShows_MaxPriceNegative_Throws() {
        ShowSearchDto dto = new ShowSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setMaxPrice(new BigDecimal("-1"));
        assertThrows(ValidationException.class, () -> validator.validateForShows(dto));
    }

    @Test
    void validateForShows_MinPriceGreaterThanMaxPrice_Throws() {
        ShowSearchDto dto = new ShowSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setMinPrice(new BigDecimal("10"));
        dto.setMaxPrice(new BigDecimal("5"));
        assertThrows(ValidationException.class, () -> validator.validateForShows(dto));
    }

    @Test
    void validateForShows_ValidDto_DoesNotThrow() {
        ShowSearchDto dto = new ShowSearchDto();
        dto.setPage(0);
        dto.setSize(10);
        dto.setName("MyShow");
        dto.setStartDate(LocalDateTime.now());
        dto.setEndDate(LocalDateTime.now().plusDays(1));
        dto.setMinPrice(new BigDecimal("5"));
        dto.setMaxPrice(new BigDecimal("10"));
        assertDoesNotThrow(() -> validator.validateForShows(dto));
    }

}

