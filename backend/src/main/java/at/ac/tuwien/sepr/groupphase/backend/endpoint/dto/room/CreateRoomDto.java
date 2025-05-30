package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room;

import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateRoomDto {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @Positive(message = "Number of rows must be positive")
    private int rows;

    @Positive(message = "Number of columns must be positive")
    private int columns;

    @NotNull(message = "Event location ID must not be null")
    private Long eventLocationId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public Long getEventLocationId() {
        return eventLocationId;
    }

    public void setEventLocationId(Long eventLocationId) {
        this.eventLocationId = eventLocationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rows, columns, eventLocationId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreateRoomDto that = (CreateRoomDto) o;
        return rows == that.rows
            && columns == that.columns
            && Objects.equals(name, that.name)
            && Objects.equals(eventLocationId, that.eventLocationId);
    }

    @Override
    public String toString() {
        return "CreateRoomDto{"
            + "name='" + name + '\''
            + ", rows=" + rows
            + ", columns=" + columns
            + ", eventLocationId=" + eventLocationId
            + '}';
    }

    public static final class CreateRoomDtoBuilder {
        private String name;
        private int rows;
        private int columns;
        private Long eventLocationId;

        private CreateRoomDtoBuilder() {
        }

        public static CreateRoomDtoBuilder aCreateRoomDtoBuilder() {
            return new CreateRoomDtoBuilder();
        }

        public CreateRoomDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CreateRoomDtoBuilder rows(int rows) {
            this.rows = rows;
            return this;
        }

        public CreateRoomDtoBuilder columns(int columns) {
            this.columns = columns;
            return this;
        }

        public CreateRoomDtoBuilder eventLocationId(Long eventLocationId) {
            this.eventLocationId = eventLocationId;
            return this;
        }

        public CreateRoomDto build() {
            CreateRoomDto dto = new CreateRoomDto();
            dto.setName(name);
            dto.setRows(rows);
            dto.setColumns(columns);
            dto.setEventLocationId(eventLocationId);
            return dto;
        }
    }
}
