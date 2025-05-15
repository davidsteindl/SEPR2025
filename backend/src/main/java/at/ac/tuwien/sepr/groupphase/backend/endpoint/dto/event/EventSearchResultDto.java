package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class EventSearchResultDto {

    @NotNull(message = "ID must not be null")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Category must not be blank")
    private String category;

    @NotNull(message = "Location ID must not be null")
    private Long locationId;


    private Integer duration;


    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;


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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventSearchResultDto that)) {
            return false;
        }
        return id.equals(that.id) && name.equals(that.name) && category.equals(that.category)
            && locationId.equals(that.locationId) && duration.equals(that.duration)
            && description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return id.hashCode() + name.hashCode() + category.hashCode() + locationId.hashCode()
            + duration.hashCode() + description.hashCode();
    }

    @Override
    public String toString() {
        return "EventSearchResultDto{"
            +
            "id=" + id
            +
            ", name='" + name + '\''
            +
            ", category='" + category + '\''
            +
            ", locationId=" + locationId
            +
            ", duration=" + duration
            +
            ", description='" + description + '\''
            +
            '}';
    }


    public static final class EventSearchResultDtoBuilder {
        private Long id;
        private String name;
        private String category;
        private Long locationId;
        private Integer duration;
        private String description;

        public static EventSearchResultDtoBuilder anEventSearchResultDto() {
            return new EventSearchResultDtoBuilder();
        }

        public EventSearchResultDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EventSearchResultDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EventSearchResultDtoBuilder category(String category) {
            this.category = category;
            return this;
        }

        public EventSearchResultDtoBuilder locationId(Long locationId) {
            this.locationId = locationId;
            return this;
        }

        public EventSearchResultDtoBuilder duration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public EventSearchResultDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public EventSearchResultDto build() {
            EventSearchResultDto searchResultDto = new EventSearchResultDto();
            searchResultDto.setId(id);
            searchResultDto.setName(name);
            searchResultDto.setCategory(category);
            searchResultDto.setLocationId(locationId);
            searchResultDto.setDuration(duration);
            searchResultDto.setDescription(description);
            return searchResultDto;
        }

    }
}
