package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event;

import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public class EventCategoryDto {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Display name must not be blank")
    private String displayName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventCategoryDto that)) {
            return false;
        }
        return name.equals(that.name) && displayName.equals(that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, displayName);
    }

    @Override
    public String toString() {
        return "EventCategoryDto{"
            + "name='" + name + '\''
            + ", displayName='" + displayName + '\''
            + '}';
    }

    public static final class EventCategoryDtoBuilder {
        private String name;
        private String displayName;

        private EventCategoryDtoBuilder() {
        }

        public static EventCategoryDtoBuilder anEventCategoryDto() {
            return new EventCategoryDtoBuilder();
        }

        public EventCategoryDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EventCategoryDtoBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public EventCategoryDto build() {
            EventCategoryDto dto = new EventCategoryDto();
            dto.setName(name);
            dto.setDisplayName(displayName);
            return dto;
        }
    }
}
