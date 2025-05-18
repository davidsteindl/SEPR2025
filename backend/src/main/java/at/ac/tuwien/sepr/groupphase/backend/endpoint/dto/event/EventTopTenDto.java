package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

public class EventTopTenDto {

    @NotNull(message = "ID must not be null")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotNull(message = "Date must not be null")
    private LocalDateTime date;

    @Min(value = 0, message = "Tickets sold must be at least 0")
    @NotNull(message = "Tickets sold must not be null")
    private Long ticketsSold;

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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(Long ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventTopTenDto that)) {
            return false;
        }
        return Objects.equals(id, that.id)
            && Objects.equals(name, that.name)
            && Objects.equals(date, that.date)
            && Objects.equals(ticketsSold, that.ticketsSold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, date, ticketsSold);
    }

    @Override
    public String toString() {
        return "EventTopTenDto{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", date=" + date
            + ", ticketsSold=" + ticketsSold
            + '}';
    }

    public static final class EventTopTenDtoBuilder {
        private Long id;
        private String name;
        private LocalDateTime date;
        private Long ticketsSold;

        private EventTopTenDtoBuilder() {
        }

        public static EventTopTenDtoBuilder anEventTopTenDto() {
            return new EventTopTenDtoBuilder();
        }

        public EventTopTenDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EventTopTenDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EventTopTenDtoBuilder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public EventTopTenDtoBuilder ticketsSold(Long ticketsSold) {
            this.ticketsSold = ticketsSold;
            return this;
        }

        public EventTopTenDto build() {
            EventTopTenDto dto = new EventTopTenDto();
            dto.setId(id);
            dto.setName(name);
            dto.setDate(date);
            dto.setTicketsSold(ticketsSold);
            return dto;
        }
    }
}
