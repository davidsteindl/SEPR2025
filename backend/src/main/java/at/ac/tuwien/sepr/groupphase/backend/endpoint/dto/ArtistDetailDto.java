package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.Set;

public class ArtistDetailDto {

    @NotNull(message = "ID must not be null")
    private Long id;

    private String firstname;

    private String lastname;

    private String stagename;

    @NotNull(message = "Shows must not be null")
    private Set<Show> shows;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        if (!(o instanceof ArtistDetailDto that)) {
            return false;
        }
        return id.equals(that.id)
            && firstname.equals(that.firstname)
            && lastname.equals(that.lastname)
            && stagename.equals(that.stagename)
            && shows.equals(that.shows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstname, lastname, stagename, shows);
    }

    @Override
    public String toString() {
        return "ArtistDetailDto{"
            + "id=" + id
            + ", firstname='" + firstname + '\''
            + ", lastname='" + lastname + '\''
            + ", stagename='" + stagename + '\''
            + ", shows=" + shows
            + '}';
    }

    public static final class ArtistDetailDtoBuilder {
        private Long id;
        private String firstname;
        private String lastname;
        private String stagename;
        private Set<Show> shows;

        private ArtistDetailDtoBuilder() {
        }

        public static ArtistDetailDtoBuilder anArtistDetailDto() {
            return new ArtistDetailDtoBuilder();
        }

        public ArtistDetailDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ArtistDetailDtoBuilder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public ArtistDetailDtoBuilder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public ArtistDetailDtoBuilder stagename(String stagename) {
            this.stagename = stagename;
            return this;
        }

        public ArtistDetailDtoBuilder shows(Set<Show> shows) {
            this.shows = shows;
            return this;
        }

        public ArtistDetailDto build() {
            ArtistDetailDto artistDetailDto = new ArtistDetailDto();
            artistDetailDto.setId(id);
            artistDetailDto.setFirstname(firstname);
            artistDetailDto.setLastname(lastname);
            artistDetailDto.setStagename(stagename);
            artistDetailDto.setShows(shows);
            return artistDetailDto;
        }
    }
}
