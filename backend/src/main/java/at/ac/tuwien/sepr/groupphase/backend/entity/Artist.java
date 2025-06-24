package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String firstname;

    @Column(length = 100)
    private String lastname;

    @Column(length = 100)
    private String stagename;

    @ManyToMany
    @JoinTable(
        name = "features",
        joinColumns = @JoinColumn(name = "artist_id"),
        inverseJoinColumns = @JoinColumn(name = "show_id")
    )
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
        this.shows = shows != null ? new HashSet<>(shows) : new HashSet<>();
    }

    public void addShow(Show show) {
        if (this.shows == null) {
            this.shows = new HashSet<>();
        }
        if (this.shows.add(show)) {
            show.addArtist(this);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Artist artist)) {
            return false;
        }
        return Objects.equals(id, artist.id)
            && Objects.equals(firstname, artist.firstname)
            && Objects.equals(lastname, artist.lastname)
            && Objects.equals(stagename, artist.stagename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstname, lastname, stagename);
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("Artist{"
            + "id=" + id
            + ", firstname='" + firstname + '\''
            + ", lastname='" + lastname + '\''
            + ", stagename='" + stagename + '\'');

        int count = 1;
        for (Show show : shows) {
            ret.append(", ").append(count).append(". show ID='").append(show.getId()).append('\'');
            count++;
        }

        ret.append('}');
        return ret.toString();
    }

    public static final class ArtistBuilder {
        private String firstname;
        private String lastname;
        private String stagename;
        private Set<Show> shows;

        private ArtistBuilder() {
        }

        public static ArtistBuilder anArtist() {
            return new ArtistBuilder();
        }

        public ArtistBuilder withFirstname(String firstname) {
            this.firstname = firstname;
            return this;
        }

        public ArtistBuilder withLastname(String lastname) {
            this.lastname = lastname;
            return this;
        }

        public ArtistBuilder withStagename(String stagename) {
            this.stagename = stagename;
            return this;
        }

        public ArtistBuilder withShows(Set<Show> shows) {
            this.shows = shows;
            return this;
        }

        public Artist build() {
            Artist artist = new Artist();
            artist.setFirstname(firstname);
            artist.setLastname(lastname);
            artist.setStagename(stagename);
            artist.setShows(shows);
            return artist;
        }
    }
}