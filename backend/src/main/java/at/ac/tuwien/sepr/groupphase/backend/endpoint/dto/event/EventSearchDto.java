package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class EventSearchDto {

    @NotNull(message = "Page index must not be null")
    @Min(value = 0, message = "Page index must be non-negative")
    private Integer page = 0;

    @NotNull(message = "Page size must not be null")
    @Min(value = 1, message = "Page size must be at least 1")
    private Integer size = 10;

    private String name;

    private String type;

    private String description;

    @Min(value = 0, message = "Duration must be non-negative")
    private Integer duration;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
