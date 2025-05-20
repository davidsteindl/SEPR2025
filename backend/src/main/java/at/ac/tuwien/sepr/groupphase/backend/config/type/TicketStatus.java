package at.ac.tuwien.sepr.groupphase.backend.config.type;

public enum TicketStatus {
    RESERVED,           // customer explicitly confirmed a hold (e.g. “I want these, I’ll pay later”)
    BOUGHT,
    REFUNDED,
    EXPIRED,            // either a hold expired or a reservation timed out
    CANCELLED,          // user-initiated cancellation of a reservation or purchase

}
