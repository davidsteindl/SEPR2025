package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ArtistSearchDto {

    @NotNull(message = "Page index must not be null")
    @Min(value = 0, message = "Page index must be non-negative")
    private Integer page = 0;

    @NotNull(message = "Page size must not be null")
    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size = 10;

    private String firstname;

    private String lastname;

    private String stagename;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
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
    public String toString() {
        return "ArtistSearchDto{"
            + "page=" + page
            + ", size=" + size
            + ", firstname='" + firstname + '\''
            + ", lastname='" + lastname + '\''
            + ", stagename='" + stagename + '\''
            + '}';
    }
}
