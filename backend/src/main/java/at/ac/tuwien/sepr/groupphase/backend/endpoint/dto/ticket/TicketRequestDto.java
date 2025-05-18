package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import java.util.List;

/**
 * Data Transfer Object for initiating ticket operations (purchase or reservation) for a specific show.
 *
 * <p>
 * Clients populate this DTO when selecting one or more ticket targets via the graphical seat map
 * (seated or standing sectors). The service will either create a payment session (for immediate purchase)
 * or hold the selected seats for later confirmation (reservation).
 *
 * <ul>
 *   <li><strong>Seated targets</strong>: {@code TicketTargetSeatedDto} specifying sector, row, and seat IDs.</li>
 *   <li><strong>Standing targets</strong>: {@code TicketTargetStandingDto} specifying sector ID and quantity.</li>
 * </ul>
 *
 * @see TicketTargetSeatedDto
 * @see TicketTargetStandingDto
 */
public class TicketRequestDto {
    private List<TicketTargetDto> targets;
    private Long showId;

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public List<TicketTargetDto> getTargets() {
        return targets;
    }

    public void setTargets(List<TicketTargetDto> targets) {
        this.targets = targets;
    }
}
