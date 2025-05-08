package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateEventDto {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Category must not be blank")
    private String category;

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
            + ", locationId=" + locationId
            + '}';
    }

    public static final class CreateEventDtoBuilder {
        private String name;
        private String category;
        private Long locationId;

        private CreateEventDtoBuilder() {
        }

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

        public CreateEventDtoBuilder locationId(Long locationId) {
            this.locationId = locationId;
            return this;
        }

        public CreateEventDto build() {
            CreateEventDto createEventDto = new CreateEventDto();
            createEventDto.setName(name);
            createEventDto.setCategory(category);
            createEventDto.setLocationId(locationId);
            return createEventDto;
        }
    }
}
