package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class ArtistSearchResultDto {

    @NotNull(message = "ID must not be null")
    private Long id;

    @Size(max = 50, message = "Firstname must not exceed 50 characters")
    private String firstname;

    @Size(max = 50, message = "Lastname must not exceed 50 characters")
    private String lastname;

    @Size(max = 50, message = "Stagename must not exceed 50 characters")
    private String stagename;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtistSearchResultDto that)) return false;
        return Objects.equals(id, that.id)
            && Objects.equals(firstname, that.firstname)
            && Objects.equals(lastname, that.lastname)
            && Objects.equals(stagename, that.stagename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstname, lastname, stagename);
    }

    @Override
    public String toString() {
        return "ArtistSearchResultDto{" +
            "id=" + id +
            ", firstname='" + firstname + '\'' +
            ", lastname='" + lastname + '\'' +
            ", stagename='" + stagename + '\'' +
            '}';
    }

    public static final class ArtistSearchResultDtoBuilder {
        private Long id;
        private String firstname;
        private String lastname;
        private String stagename;

        public static ArtistSearchResultDtoBuilder anArtistSearchResultDto() {
            return new ArtistSearchResultDtoBuilder();
        }

        public ArtistSearchResultDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ArtistSearchResultDtoBuilder firstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public ArtistSearchResultDtoBuilder lastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public ArtistSearchResultDtoBuilder stagename(String stagename) {
            this.stagename = stagename;
            return this;
        }

        public ArtistSearchResultDto build() {
            ArtistSearchResultDto dto = new ArtistSearchResultDto();
            dto.setId(id);
            dto.setFirstname(firstname);
            dto.setLastname(lastname);
            dto.setStagename(stagename);
            return dto;
        }
    }
}
