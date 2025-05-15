package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowDetailDto;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

public class EventWithShowsDto {

    @NotNull(message = "Event must not be null")
    private EventDetailDto event;

    @NotNull(message = "Shows must not be null")
    private List<ShowDetailDto> shows;

    public EventDetailDto getEvent() {
        return event;
    }

    public void setEvent(EventDetailDto event) {
        this.event = event;
    }

    public List<ShowDetailDto> getShows() {
        return shows;
    }

    public void setShows(List<ShowDetailDto> shows) {
        this.shows = shows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventWithShowsDto that)) {
            return false;
        }
        return Objects.equals(event, that.event)
            && Objects.equals(shows, that.shows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, shows);
    }

    @Override
    public String toString() {
        return "EventWithShowsDto{"
            + "event=" + event
            + ", shows=" + shows
            + '}';
    }

    public static final class EventWithShowsDtoBuilder {
        private EventDetailDto event;
        private List<ShowDetailDto> shows;

        private EventWithShowsDtoBuilder() {
        }

        public static EventWithShowsDtoBuilder anEventWithShowsDto() {
            return new EventWithShowsDtoBuilder();
        }

        public EventWithShowsDtoBuilder event(EventDetailDto event) {
            this.event = event;
            return this;
        }

        public EventWithShowsDtoBuilder shows(List<ShowDetailDto> shows) {
            this.shows = shows;
            return this;
        }

        public EventWithShowsDto build() {
            EventWithShowsDto dto = new EventWithShowsDto();
            dto.setEvent(event);
            dto.setShows(shows);
            return dto;
        }
    }
}
