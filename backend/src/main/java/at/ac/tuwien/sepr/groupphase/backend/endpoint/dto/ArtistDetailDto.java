package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.Set;

public class ArtistDetailDto {

    @NotNull(message = "ID must not be null")
    private Long id;

    private String firstname;

    private String lastname;

    private String stagename;

    @NotNull(message = "Show IDs must not be null")
    private Set<Long> showIds;

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
        if (!(o instanceof ArtistDetailDto that)) {
            return false;
        }
        return id.equals(that.id)
            && firstname.equals(that.firstname)
            && lastname.equals(that.lastname)
            && stagename.equals(that.stagename)
            && showIds.equals(that.showIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstname, lastname, stagename, showIds);
    }

    @Override
    public String toString() {
        return "ArtistDetailDto{"
            + "id=" + id
            + ", firstname='" + firstname + '\''
            + ", lastname='" + lastname + '\''
            + ", stagename='" + stagename + '\''
            + ", shows=" + showIds
            + '}';
    }

    public static final class ArtistDetailDtoBuilder {
        private Long id;
        private String firstname;
        private String lastname;
        private String stagename;
        private Set<Long> showIds;

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

        public ArtistDetailDtoBuilder showIds(Set<Long> shows) {
            this.showIds = shows;
            return this;
        }

        public ArtistDetailDto build() {
            ArtistDetailDto artistDetailDto = new ArtistDetailDto();
            artistDetailDto.setId(id);
            artistDetailDto.setFirstname(firstname);
            artistDetailDto.setLastname(lastname);
            artistDetailDto.setStagename(stagename);
            artistDetailDto.setShowIds(showIds);
            return artistDetailDto;
        }
    }
}
