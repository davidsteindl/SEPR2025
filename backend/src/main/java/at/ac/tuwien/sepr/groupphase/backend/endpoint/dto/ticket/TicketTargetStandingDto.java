package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;

/**
 * Data Transfer Object representing a standing-sector ticket selection.
 *
 * <p>
 * Used when the customer chooses a number of standing-room tickets in a particular sector
 * via the graphical hall map for purchase or reservation. Each instance ties the request
 * to a specific standing-sector layout and the desired ticket quantity.
 *
 * @see StandingSector
 * @see TicketRequestDto
 */
public class TicketTargetStandingDto implements TicketTargetDto {
    private Long sectorId;
    private int quantity;

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
