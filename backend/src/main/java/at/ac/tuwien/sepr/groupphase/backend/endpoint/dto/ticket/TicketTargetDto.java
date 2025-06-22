package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"        // <-- this JSON field tells Jackson which subtype to use
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TicketTargetSeatedDto.class,   name = "seated"),
    @JsonSubTypes.Type(value = TicketTargetStandingDto.class, name = "standing")
})
public interface TicketTargetDto {
    


}
