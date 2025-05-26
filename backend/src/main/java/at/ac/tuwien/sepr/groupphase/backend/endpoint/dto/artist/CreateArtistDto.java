package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.annotation.ValidNameCombination;
import jakarta.validation.constraints.Size;

import java.util.Objects;
import java.util.Set;

@ValidNameCombination
public class CreateArtistDto {

    @Size(max = 50, message = "Firstname must not exceed 50 characters")
    private String firstname;

    @Size(max = 50, message = "Lastname must not exceed 50 characters")
    private String lastname;

    @Size(max = 50, message = "Stagename must not exceed 50 characters")
    private String stagename;

    private Set<Long> showIds;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getStagename() {
        return stagename;
    }

    public void setStagename(String stagename) {
        this.stagename = stagename;
    }

    public Set<Long> getShowIds() {
        return showIds;
    }

    public void setShowIds(Set<Long> showIds) {
        this.showIds = showIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CreateArtistDto that)) {
            return false;
        }
        return Objects.equals(firstname, that.firstname)
            && Objects.equals(lastname, that.lastname)
            && Objects.equals(stagename, that.stagename)
            && Objects.equals(showIds, that.showIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstname, lastname, stagename, showIds);
    }

    @Override
    public String toString() {
        return "CreateArtistDto{"
            + "firstname='" + firstname + '\''
            + ", lastname='" + lastname + '\''
            + ", stagename='" + stagename + '\''
            + ", shows=" + showIds
            + '}';
    }

    public static final class CreateArtistDtoBuilder {
        private String firstname;
        private String lastname;
        private String stagename;
        private Set<Long> showIds;

        private CreateArtistDtoBuilder() {
        }

        public static CreateArtistDtoBuilder aCreateArtistDto() {
            return new CreateArtistDtoBuilder();
        }

        public CreateArtistDtoBuilder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public CreateArtistDtoBuilder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public CreateArtistDtoBuilder stagename(String stagename) {
            this.stagename = stagename;
            return this;
        }

        public CreateArtistDtoBuilder showIds(Set<Long> shows) {
            this.showIds = shows;
            return this;
        }

        public CreateArtistDto build() {
            CreateArtistDto createArtistDto = new CreateArtistDto();
            createArtistDto.setFirstname(firstname);
            createArtistDto.setLastname(lastname);
            createArtistDto.setStagename(stagename);
            createArtistDto.setShowIds(showIds);
            return createArtistDto;
        }
    }
}
