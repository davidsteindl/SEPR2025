package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

public class CreateShowDto {

    @NotNull(message = "Duration must not be null")
    @Min(value = 10, message = "Duration must be at least 10 minutes")
    @Max(value = 600, message = "Duration must be at most 600 minutes")
    private int duration;

    @NotNull(message = "Date must not be null")
    @FutureOrPresent(message = "Date must be in the present or future")
    private LocalDateTime date;

    @NotNull(message = "Event ID must not be null")
    private Long eventId;

    @NotNull(message = "Artist IDs must not be null")
    @Size(min = 1, message = "Show must have at least one artist")
    private Set<Long> artistIds;

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
        if (!(o instanceof CreateShowDto show)) {
            return false;
        }
        return duration == show.duration
            && date.equals(show.date)
            && eventId.equals(show.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration, date, eventId);
    }

    @Override
    public String toString() {
        return "CreateShowDto{"
            + "duration=" + duration
            + ", date=" + date
            + ", eventId=" + eventId
            + ", artistIds=" + artistIds
            + '}';
    }

    public static final class CreateShowDtoBuilder {
        private int duration;
        private LocalDateTime date;
        private Long eventId;
        private Set<Long> artistIds;

        private CreateShowDtoBuilder() {
        }

        public static CreateShowDtoBuilder aCreateShowDto() {
            return new CreateShowDtoBuilder();
        }

        public CreateShowDtoBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public CreateShowDtoBuilder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public CreateShowDtoBuilder eventId(Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public CreateShowDtoBuilder artistIds(Set<Long> artistIds) {
            this.artistIds = artistIds;
            return this;
        }

        public CreateShowDto build() {
            CreateShowDto createShowDto = new CreateShowDto();
            createShowDto.setDuration(duration);
            createShowDto.setDate(date);
            createShowDto.setEventId(eventId);
            createShowDto.setArtistIds(artistIds);
            return createShowDto;
        }
    }
}
