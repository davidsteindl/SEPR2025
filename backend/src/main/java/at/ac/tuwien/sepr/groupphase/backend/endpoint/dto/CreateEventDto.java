package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class CreateEventDto {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Category must not be blank")
    private String category;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @Min(value = 10, message = "Duration must be at least 10 minutes")
    @Max(value = 10000, message = "Duration must not exceed 10000 minutes")
    private int duration;

    @NotNull(message = "Location ID must not be null")
    private Long locationId;

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
    public String toString() {
        return "CreateEventDto{"
            + "name='" + name + '\''
            + ", category='" + category + '\''
            + ", description='" + description + '\''
            + ", duration=" + duration
            + ", locationId=" + locationId
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CreateEventDto that)) {
            return false;
        }
        return duration == that.duration
            && Objects.equals(name, that.name)
            && Objects.equals(category, that.category)
            && Objects.equals(description, that.description)
            && Objects.equals(locationId, that.locationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, description, duration, locationId);
    }

    public static final class CreateEventDtoBuilder {
        private String name;
        private String category;
        private String description;
        private int duration;
        private Long locationId;

        private CreateEventDtoBuilder() {}

        public static CreateEventDtoBuilder aCreateEventDto() {
            return new CreateEventDtoBuilder();
        }

        public CreateEventDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CreateEventDtoBuilder category(String category) {
            this.category = category;
            return this;
        }

        public CreateEventDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CreateEventDtoBuilder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public CreateEventDtoBuilder locationId(Long locationId) {
            this.locationId = locationId;
            return this;
        }

        public CreateEventDto build() {
            CreateEventDto dto = new CreateEventDto();
            dto.setName(name);
            dto.setCategory(category);
            dto.setDescription(description);
            dto.setDuration(duration);
            dto.setLocationId(locationId);
            return dto;
        }
    }
}
