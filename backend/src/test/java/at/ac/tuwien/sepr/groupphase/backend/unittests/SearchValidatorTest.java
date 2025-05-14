package at.ac.tuwien.sepr.groupphase.backend.unittests;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.SearchValidator;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void validateForEvents_NullDto_Throws() {
        assertThrows(ValidationException.class, () -> validator.validateForEvents(null));
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
        dto.setType("UnknownType");
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
        dto.setType("Rock");
        dto.setDescription("A show");
        dto.setDuration(120);
        assertDoesNotThrow(() -> validator.validateForEvents(dto));
    }
}

