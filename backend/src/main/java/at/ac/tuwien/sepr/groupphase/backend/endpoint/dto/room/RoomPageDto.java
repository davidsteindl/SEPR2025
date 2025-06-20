package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class RoomPageDto {
    @NotNull(message = "ID must not be null")
    @Positive(message = "ID must be positive")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Event location name must not be blank")
    private String eventLocationName;

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

    public String getEventLocationName() {
        return eventLocationName;
    }

    public void setEventLocationName(String eventLocationName) {
        this.eventLocationName = eventLocationName;
    }

    public static final class RoomPageDtoBuilder {
        private Long id;
        private String name;
        private String eventLocationName;

        private RoomPageDtoBuilder() { }

        public static RoomPageDtoBuilder aRoomPageDto() {
            return new RoomPageDtoBuilder();
        }

        public RoomPageDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoomPageDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomPageDtoBuilder eventLocationName(String eventLocationName) {
            this.eventLocationName = eventLocationName;
            return this;
        }

        public RoomPageDto build() {
            RoomPageDto dto = new RoomPageDto();
            dto.setId(id);
            dto.setName(name);
            dto.setEventLocationName(eventLocationName);
            return dto;
        }
    }
}
