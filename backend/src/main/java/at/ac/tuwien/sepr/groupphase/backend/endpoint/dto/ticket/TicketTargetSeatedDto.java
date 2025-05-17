package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

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
public class TicketTargetSeatedDto implements TicketTargetDto {
    private Long sectorId;
    private Long seatId;
}
