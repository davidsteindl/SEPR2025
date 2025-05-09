package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

public class ShowDetailDto {

    @NotNull(message = "ID must not be null")
    private Long id;

    @NotNull(message = "Duration must not be null")
    private int duration;

    @NotNull(message = "Date must not be null")
    private LocalDateTime date;

    @NotNull(message = "Event ID must not be null")
    private Long eventId;

    @NotNull(message = "Artist IDs must not be null")
    private Set<Long> artistIds;

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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Set<Long> getArtistIds() {
        return artistIds;
    }

    public void setArtistIds(Set<Long> artistIds) {
        this.artistIds = artistIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShowDetailDto show)) {
            return false;
        }
        return duration == show.duration
            && id.equals(show.id)
            && date.equals(show.date)
            && eventId.equals(show.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, duration, date, eventId);
    }

    @Override
    public String toString() {
        return "ShowDetailDto{"
            + "id=" + id
            + ", duration=" + duration
            + ", date=" + date
            + ", eventId=" + eventId
            + ", artistIds=" + artistIds
            + '}';
    }

    public static final class ShowDetailDtoBuilder {
        private Long id;
        private int duration;
        private LocalDateTime date;
        private Long eventId;
        private Set<Long> artistIds;

        private ShowDetailDtoBuilder() {
        }

        public static ShowDetailDtoBuilder aShowDetailDto() {
            return new ShowDetailDtoBuilder();
        }

        public ShowDetailDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ShowDetailDtoBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public ShowDetailDtoBuilder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public ShowDetailDtoBuilder eventId(Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public ShowDetailDtoBuilder artistIds(Set<Long> artistIds) {
            this.artistIds = artistIds;
            return this;
        }

        public ShowDetailDto build() {
            ShowDetailDto showDetailDto = new ShowDetailDto();
            showDetailDto.setId(id);
            showDetailDto.setDuration(duration);
            showDetailDto.setDate(date);
            showDetailDto.setEventId(eventId);
            showDetailDto.setArtistIds(artistIds);
            return showDetailDto;
        }
    }
}
