package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public class ArtistDto {

    private Long id;
    private String firstname;
    private String lastname;
    private String stagename;

    public ArtistDto() {
    }

    public ArtistDto(Long id, String firstname, String lastname, String stagename) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.stagename = stagename;
    }

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
}