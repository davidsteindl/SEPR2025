package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.room;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SeatDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public class RoomDetailDto {

    @NotNull(message = "ID must not be null")
    @Positive(message = "ID must be positive")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotNull(message = "Sectors must not be null")
    private List<@Valid SectorDto> sectors;

    @NotNull(message = "Seats must not be null")
    private List<@Valid SeatDto> seats;

    @NotNull(message = "Event location ID must not be null")
    private Long eventLocationId;

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

    public List<SectorDto> getSectors() {
        return sectors;
    }

    public void setSectors(List<SectorDto> sectors) {
        this.sectors = sectors;
    }

    public List<SeatDto> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDto> seats) {
        this.seats = seats;
    }

    public Long getEventLocationId() {
        return eventLocationId;
    }

    public void setEventLocationId(Long eventLocationId) {
        this.eventLocationId = eventLocationId;
    }

    public static final class RoomDetailDtoBuilder {
        private Long id;
        private String name;
        private List<SectorDto> sectors;
        private List<SeatDto> seats;
        private Long eventLocationId;

        private RoomDetailDtoBuilder() {
        }

        public static RoomDetailDtoBuilder aRoomDetailDto() {
            return new RoomDetailDtoBuilder();
        }

        public RoomDetailDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoomDetailDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoomDetailDtoBuilder sectors(List<SectorDto> sectors) {
            this.sectors = sectors;
            return this;
        }

        public RoomDetailDtoBuilder seats(List<SeatDto> seats) {
            this.seats = seats;
            return this;
        }

        public RoomDetailDtoBuilder eventLocationId(Long eventLocationId) {
            this.eventLocationId = eventLocationId;
            return this;
        }

        public RoomDetailDto build() {
            RoomDetailDto dto = new RoomDetailDto();
            dto.setId(id);
            dto.setName(name);
            dto.setSectors(sectors);
            dto.setSeats(seats);
            dto.setEventLocationId(eventLocationId);
            return dto;
        }
    }
}
