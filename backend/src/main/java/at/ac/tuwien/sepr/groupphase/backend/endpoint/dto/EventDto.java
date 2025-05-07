package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;

public class EventDto {
    @NotNull(message = "ID must not be null")
    private Long id;
    @NotNull(message = "Name must not be null")
    private String name;
    @NotNull(message = "Category must not be null")
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
        if (!(o instanceof EventDto that)) {
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

        public EventDto build() {
            EventDto eventDto = new EventDto();
            eventDto.setId(id);
            eventDto.setName(name);
            eventDto.setCategory(category);
            eventDto.setLocationId(locationId);
            return eventDto;
        }
    }
}
