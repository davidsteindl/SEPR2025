package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EventDetailDto {

    @NotNull(message = "ID must not be null")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Category must not be blank")
    private String category;

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
        return id.equals(that.id)
            && name.equals(that.name)
            && category.equals(that.category)
            && locationId.equals(that.locationId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, category, locationId);
    }

    @Override
    public String toString() {
        return "EventDto{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", category='" + category + '\''
            + ", locationId=" + locationId
            + '}';
    }

    public static final class EventDtoBuilder {
        private Long id;
        private String name;
        private String category;
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

        public EventDtoBuilder locationId(Long locationId) {
            this.locationId = locationId;
            return this;
        }

        public EventDetailDto build() {
            EventDetailDto eventDetailDto = new EventDetailDto();
            eventDetailDto.setId(id);
            eventDetailDto.setName(name);
            eventDetailDto.setCategory(category);
            eventDetailDto.setLocationId(locationId);
            return eventDetailDto;
        }
    }
}
