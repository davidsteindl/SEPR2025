package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(nullable = false)
    @Min(10)
    @Max(600)
    private int duration;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToMany(mappedBy = "shows")
    private Set<Artist> artists;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Set<Artist> getArtists() {
        return artists;
    }

    public void setArtists(Set<Artist> artists) {
        this.artists = new HashSet<>();
        if (artists != null) {
            for (Artist artist : artists) {
                this.addArtist(artist);
            }
        }
    }

    public void addArtist(Artist artist) {
        if (this.artists == null) {
            this.artists = new HashSet<>();
        }
        if (this.artists.add(artist)) {
            artist.addShow(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Show show)) {
            return false;
        }
        return duration == show.duration
            && Objects.equals(id, show.id)
            && Objects.equals(name, show.name)
            && Objects.equals(date, show.date)
            && Objects.equals(event, show.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, duration, date, event);
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("Show{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", duration=" + duration + " min"
            + ", date=" + date
            + ", event=" + (event != null ? event.getId() : "null"));

        int count = 1;
        for (Artist artist : artists) {
            ret.append(", ").append(count).append(". artist ID='").append(artist.getId()).append('\'');
            count++;
        }

        ret.append('}');
        return ret.toString();
    }

    public static final class ShowBuilder {
        private String name;
        private int duration;
        private LocalDateTime date;
        private Event event;
        private Set<Artist> artists;

        private ShowBuilder() {
        }

        public static ShowBuilder aShow() {
            return new ShowBuilder();
        }

        public ShowBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ShowBuilder withDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public ShowBuilder withDate(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public ShowBuilder withEvent(Event event) {
            this.event = event;
            return this;
        }

        public ShowBuilder withArtists(Set<Artist> artists) {
            this.artists = artists;
            return this;
        }

        public Show build() {
            Show show = new Show();
            show.setName(name);
            show.setDuration(duration);
            show.setDate(date);
            show.setEvent(event);
            show.setArtists(artists);
            return show;
        }
    }
}