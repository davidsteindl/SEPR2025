package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Data Transfer Object representing a specific seat selection within a seated sector.
 *
 * <p>
 * Used when the customer chooses one or more individual seats via the graphical hall map
 * for purchase or reservation. Each instance ties a target operation to a particular
 * sector layout and seat identifier.
 *
 * @see TicketTargetStandingDto
 * @see TicketRequestDto
 */
@JsonTypeName("seated")
public class TicketTargetSeatedDto implements TicketTargetDto {
    private Long sectorId;
    private Long seatId;

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }
}
