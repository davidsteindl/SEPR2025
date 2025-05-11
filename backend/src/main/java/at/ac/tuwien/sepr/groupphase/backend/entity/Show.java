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

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDateTime getDateTime() {
        return date;
    }

    public void setDateTime(LocalDateTime date) {
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
        this.artists = artists;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Show show)) {
            return false;
        }
        return Objects.equals(id, show.id)
            && duration == show.duration
            && Objects.equals(date, show.date)
            && Objects.equals(event, show.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, duration, date, event);
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("Show{"
            + "id=" + id
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
        private int duration;
        private LocalDateTime date;
        private Event event;
        private Set<Artist> artists;

        private ShowBuilder() {
        }

        public static ShowBuilder aShow() {
            return new ShowBuilder();
        }

        public ShowBuilder withDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public ShowBuilder withDateTime(LocalDateTime date) {
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
            show.setDuration(duration);
            show.setDateTime(date);
            show.setEvent(event);
            show.setArtists(artists);
            return show;
        }
    }
}