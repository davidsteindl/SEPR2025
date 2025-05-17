package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

/**
 * Represents the target selection for a ticket purchase or reservation.
 *
 * <p>
 * Implementations specify the exact type of ticket target, such as:
 * <ul>
 *     <li>a specific seat within a seated sector, or</li>
 *     <li>a slot within a standing sector.</li>
 * </ul>
 * This is used in scenarios where customers choose a particular seat or a standing slot when buying or reserving tickets.
 */
public interface TicketTargetDto {

    // maybe this would be an amazing place to use the observer pattern or mehrfaches dynamisches binden


}
