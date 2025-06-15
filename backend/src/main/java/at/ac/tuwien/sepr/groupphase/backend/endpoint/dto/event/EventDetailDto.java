package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class EventDetailDto {

    @NotNull(message = "ID must not be null")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Category must not be blank")
    private String category;


    @NotBlank(message = "Description must not be blank")
    private String description;

    @NotNull(message = "Start date and time must not be null")
    private LocalDateTime dateTime;

    @Min(value = 10, message = "Duration must be at least 10 minutes")
    @Max(value = 10000, message = "Duration must not exceed 10000 minutes")
    private int duration;

    @NotNull(message = "Location ID must not be null")
    private Long locationId;


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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventDetailDto that)) {
            return false;
        }
        return duration == that.duration
            && id.equals(that.id)
            && name.equals(that.name)
            && category.equals(that.category)
            && description.equals(that.description)
            && dateTime.equals(that.dateTime)
            && locationId.equals(that.locationId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, category, description, duration, locationId);
    }

    @Override
    public String toString() {
        return "EventDetailDto{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", category='" + category + '\''
            + ", description='" + description + '\''
            + ", dateTime=" + dateTime
            + ", duration=" + duration
            + ", locationId=" + locationId
            + '}';
    }

    public static final class EventDtoBuilder {
        private Long id;
        private String name;
        private String category;
        private String description;
        private LocalDateTime dateTime;
        private int duration;
        private Long locationId;

        private EventDtoBuilder() {
        }

        public static EventDtoBuilder anEventDto() {
            return new EventDtoBuilder();
        }

        public EventDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EventDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EventDtoBuilder category(String category) {
            this.category = category;
            return this;
        }

        public EventDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public EventDtoBuilder dateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public EventDtoBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public EventDtoBuilder locationId(Long locationId) {
            this.locationId = locationId;
            return this;
        }

        public EventDetailDto build() {
            EventDetailDto dto = new EventDetailDto();
            dto.setId(id);
            dto.setName(name);
            dto.setCategory(category);
            dto.setDescription(description);
            dto.setDateTime(dateTime);
            dto.setDuration(duration);
            dto.setLocationId(locationId);
            return dto;
        }
    }
}