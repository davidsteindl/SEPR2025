package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.Set;

public class CreateArtistDto {

    private String firstname;

    private String lastname;

    private String stagename;

    @NotNull(message = "Shows must not be null")
    private Set<Show> shows;

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

    public Set<Show> getShows() {
        return shows;
    }

    public void setShows(Set<Show> shows) {
        this.shows = shows;
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
            && Objects.equals(shows, that.shows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstname, lastname, stagename, shows);
    }

    @Override
    public String toString() {
        return "CreateArtistDto{"
            + "firstname='" + firstname + '\''
            + ", lastname='" + lastname + '\''
            + ", stagename='" + stagename + '\''
            + ", shows=" + shows
            + '}';
    }

    public static final class CreateArtistDtoBuilder {
        private String firstname;
        private String lastname;
        private String stagename;
        private Set<Show> shows;

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

        public CreateArtistDtoBuilder shows(Set<Show> shows) {
            this.shows = shows;
            return this;
        }

        public CreateArtistDto build() {
            CreateArtistDto createArtistDto = new CreateArtistDto();
            createArtistDto.setFirstname(firstname);
            createArtistDto.setLastname(lastname);
            createArtistDto.setStagename(stagename);
            createArtistDto.setShows(shows);
            return createArtistDto;
        }
    }
}
