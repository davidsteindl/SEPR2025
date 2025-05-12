package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public class ArtistSearchDto {
    private String firstname;
    private String lastname;
    private String stagename;

    public ArtistSearchDto() {
    }

    public ArtistSearchDto(String firstname, String lastname, String stagename) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.stagename = stagename;
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
        return "ArtistSearchDto{" +
            "firstname='" + firstname + '\'' +
            ", lastname='" + lastname + '\'' +
            ", stagename='" + stagename + '\'' +
            '}';
    }
}
