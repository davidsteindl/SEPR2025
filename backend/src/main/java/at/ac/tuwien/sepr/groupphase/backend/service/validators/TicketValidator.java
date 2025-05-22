package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.config.type.TicketStatus;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetSeatedDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket.TicketTargetStandingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Hold;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.entity.StandingSector;
import at.ac.tuwien.sepr.groupphase.backend.entity.ticket.Ticket;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ReservationExpiredException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ReservationNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.SeatUnavailableException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HoldRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.AuthenticationFacade;
import at.ac.tuwien.sepr.groupphase.backend.security.AuthenticationFacadeImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.RoomServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ShowServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TicketValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketValidator.class);
    private final ShowServiceImpl showService;
    private final RoomServiceImpl roomService;
    private final HoldRepository holdRepository;
    private final TicketRepository ticketRepository;
    private final AuthenticationFacade authenticationFacade;

    public TicketValidator(ShowServiceImpl showService, RoomServiceImpl roomService, HoldRepository holdRepository, TicketRepository ticketRepository, AuthenticationFacade authenticationFacade) {
        this.showService = showService;
        this.roomService = roomService;
        this.holdRepository = holdRepository;
        this.ticketRepository = ticketRepository;
        this.authenticationFacade = authenticationFacade;
    }

    public void validateForBuyTickets(TicketRequestDto dto) {
        LOGGER.debug("validateForBuyTickets: {}", dto);
        Show show = requireShow(dto.getShowId());
        validateTargetsBelongToShow(show, dto.getTargets());
        validateBeforeShowStarts(show);
        validateNoHoldsOn(dto.getShowId(), dto.getTargets());
        validateNoTicketsOn(dto.getShowId(), dto.getTargets());
    }

    public void validateHold(Long showId, Long sectorId, Long seatId, Long userId) {
        LOGGER.debug("validateHold: showId={}, sectorId={}, seatId={}, userId={}", showId, sectorId, seatId, userId);
        Show show = requireShow(showId);
        validateBeforeShowStarts(show);
        validateSectorBelongsToShow(show, sectorId);
        if (seatId != null) {
            validateSeatBelongsToSector(sectorId, seatId);
        }
        validateNoHoldsOn(showId, sectorId, seatId);
        validateNoTicketsOn(showId, sectorId, seatId);
    }

    public void validateForReserveTickets(TicketRequestDto dto) {
        LOGGER.debug("validateForReserveTickets: {}", dto);
        Show show = requireShow(dto.getShowId());
        validateTargetsBelongToShow(show, dto.getTargets());
        validateBeforeShowStarts(show);
        validateNoTicketsOn(dto.getShowId(), dto.getTargets());
    }

    public void validateForBuyReservedTickets(List<Long> ids, List<Ticket> tickets) {
        LOGGER.debug("validateForBuyReservedTickets: ticketIds={}, tickets={}", ids, tickets);
        validateListSizes(ids, tickets);
        validateTicketsStatus(tickets, TicketStatus.RESERVED);
        validateReservationStillValid(tickets);
        validateTicketsOwnedByCurrentUser(tickets);
    }

    public void validateForCancelReservations(List<Long> ids, List<Ticket> tickets) {
        LOGGER.debug("validateForCancelReservations: ticketIds={}, tickets={}", ids, tickets);
        validateListSizes(ids, tickets);
        validateTicketsStatus(tickets, TicketStatus.RESERVED);
        validateReservationStillValid(tickets);
        validateTicketsOwnedByCurrentUser(tickets);
    }

    public void validateForRefundTickets(List<Long> ids, List<Ticket> tickets) {
        LOGGER.debug("validateForRefundTickets: ticketIds={}, tickets={}", ids, tickets);
        validateListSizes(ids, tickets);
        validateTicketsStatus(tickets, TicketStatus.BOUGHT);
        validateRefundWindow(tickets);
        validateTicketsOwnedByCurrentUser(tickets);
    }

    /**
     * Ensures the given show exists (or throws).
     */
    private Show requireShow(Long showId) {
        Show show = showService.getShowById(showId);
        if (show == null) {
            throw new NotFoundException("Show with id " + showId + " not found");
        }
        return show;
    }

    /**
     * Validates all DTO targets refer to sectors (and seats, if any) in this show’s room.
     */
    private void validateTargetsBelongToShow(Show show, List<TicketTargetDto> targets) {
        for (TicketTargetDto t : targets) {
            if (t instanceof TicketTargetSeatedDto s) {
                validateSectorBelongsToShow(show, s.getSectorId());
                validateSeatBelongsToSector(s.getSectorId(), s.getSeatId());
            } else if (t instanceof TicketTargetStandingDto st) {
                validateSectorBelongsToShow(show, st.getSectorId());
                // TODO: validate quantity > 0 and not exceeding standing capacity
            }
        }
    }

    /**
     * Validates that the given sector is part of the show’s room.
     */
    private void validateSectorBelongsToShow(Show show, Long sectorId) {
        boolean ok = show.getRoom().getSectors()
            .stream()
            .anyMatch(sec -> sec.getId().equals(sectorId));
        if (!ok) {
            throw new SeatUnavailableException("Sector " + sectorId + " is not in room for show " + show.getId());
        }
    }

    /**
     * Validates that the given seat belongs to the given sector.
     */
    private void validateSeatBelongsToSector(Long sectorId, Long seatId) {
        var seat = roomService.getSeatById(seatId);
        if (!seat.getSector().getId().equals(sectorId)) {
            throw new SeatUnavailableException("Seat " + seatId + " is not in sector " + sectorId);
        }
    }

    /**
     * Validates that no one else currently holds any of these targets, and
     * that standing‐sector capacity isn’t exceeded.
     */
    private void validateNoHoldsOn(Long showId, List<TicketTargetDto> targets) {
        LocalDateTime now = LocalDateTime.now();

        // fetch all valid holds for this show that were created by *other* users
        List<Hold> otherHolds = holdRepository.findByShowId(showId).stream()
            .filter(h -> h.getValidUntil().isAfter(now))
            .filter(h -> !h.getUserId().equals(authenticationFacade.getCurrentUserId()))
            .toList();

        // group existing holds by sector for quick lookup
        Map<Long, Long> holdsBySector = otherHolds.stream()
            .filter(h -> h.getSeatId() == null)
            .collect(Collectors.groupingBy(Hold::getSectorId, Collectors.counting()));

        // fetch existing tickets (BOUGHT or RESERVED) for this show
        List<Ticket> existing = ticketRepository.findByShowId(showId).stream()
            .filter(t -> t.getStatus() == TicketStatus.BOUGHT || t.getStatus() == TicketStatus.RESERVED)
            .toList();

        // group existing tickets by sector
        Map<Long, Long> ticketsBySector = existing.stream()
            .filter(t -> t.getSeat() == null)
            .collect(Collectors.groupingBy(t -> t.getSector().getId(), Collectors.counting()));

        for (TicketTargetDto t : targets) {
            if (t instanceof TicketTargetSeatedDto s) {
                // SEATED: any OTHER valid hold on this seat?
                boolean conflict = otherHolds.stream()
                    .anyMatch(h -> s.getSeatId().equals(h.getSeatId()));
                if (conflict) {
                    throw new SeatUnavailableException("Seat " + s.getSeatId() + " is currently on hold");
                }
                // also check if already bought/reserved
                boolean occupied = existing.stream()
                    .anyMatch(tk -> tk.getSeat() != null && tk.getSeat().getId().equals(s.getSeatId()));
                if (occupied) {
                    throw new SeatUnavailableException("Seat " + s.getSeatId() + " is already taken");
                }

            } else if (t instanceof TicketTargetStandingDto st) {
                // STANDING: ensure capacity not exceeded
                long capacity = ((StandingSector) roomService.getSectorById(st.getSectorId())).getCapacity();
                long boughtOrReserved = ticketsBySector.getOrDefault(st.getSectorId(), 0L);
                long held = holdsBySector.getOrDefault(st.getSectorId(), 0L);
                long requested = st.getQuantity();

                if (boughtOrReserved + held + requested > capacity) {
                    throw new SeatUnavailableException(
                        "Not enough capacity in standing sector %d (requested %d, available %d)".formatted(st.getSectorId(), requested, capacity - boughtOrReserved - held)
                    );
                }
            }
        }
    }

    /**
     * Validates that no one holds this single seat/sector right now.
     */
    private void validateNoHoldsOn(Long showId, Long sectorId, Long seatId) {
        LocalDateTime now = LocalDateTime.now();
        List<Hold> holds = holdRepository.findByShowId(showId);
        boolean conflict = holds.stream()
            .anyMatch(h -> h.getValidUntil().isAfter(now)
                && h.getSectorId().equals(sectorId)
                && ((seatId == null && h.getSeatId() == null)
                || (h.getSeatId() != null && h.getSeatId().equals(seatId))));
        if (conflict) {
            throw new SeatUnavailableException("Seat/Sector already on hold for show " + showId);
        }
    }

    /**
     * Validates that none of these targets already have a BOUGHT or RESERVED ticket.
     */
    private void validateNoTicketsOn(Long showId, List<TicketTargetDto> targets) {
        List<Ticket> existing = ticketRepository.findByShowId(showId)
            .stream()
            .filter(t -> t.getStatus() == TicketStatus.BOUGHT || t.getStatus() == TicketStatus.RESERVED)
            .toList();
        for (TicketTargetDto t : targets) {
            if (t instanceof TicketTargetSeatedDto s) {
                boolean occupied = existing.stream()
                    .anyMatch(tk -> tk.getSeat() != null && tk.getSeat().getId().equals(s.getSeatId()));
                if (occupied) {
                    throw new SeatUnavailableException("Seat " + s.getSeatId() + " already taken");
                }
            }
            // TODO: for standing, ensure quantity does not exceed remaining spots
        }
    }

    /**
     * Validates that a single seat/sector has no BOUGHT or RESERVED ticket.
     */
    private void validateNoTicketsOn(Long showId, Long sectorId, Long seatId) {
        List<Ticket> existing = ticketRepository.findByShowId(showId)
            .stream()
            .filter(t -> t.getStatus() == TicketStatus.BOUGHT || t.getStatus() == TicketStatus.RESERVED)
            .toList();
        boolean conflict = existing.stream().anyMatch(t ->
            t.getSector().getId().equals(sectorId)
                && ((seatId == null && t.getSeat() == null)
                || (t.getSeat() != null && t.getSeat().getId().equals(seatId))));
        if (conflict) {
            throw new SeatUnavailableException("Seat/Sector already booked for show " + showId);
        }
    }

    /**
     * Validates that the current time is at least {@code minutesBefore} before the show start.
     */
    private void validateBeforeShowStarts(Show show) {
        LocalDateTime cutoff = show.getDate().minusMinutes(30);
        if (LocalDateTime.now().isAfter(cutoff)) {
            throw new SeatUnavailableException("Cannot modify tickets less than "
                + (long) 30 + " minutes before show starts");
        }
    }

    /**
     * Ensures the two lists match in size (i.e. every requested ID mapped to a ticket).
     */
    private void validateListSizes(List<Long> ids, List<Ticket> tickets) {
        if (ids.size() != tickets.size()) {
            throw new ReservationNotFoundException("Some requested tickets were not found");
        }
    }

    /**
     * Ensures all tickets have the given status.
     */
    private void validateTicketsStatus(List<Ticket> tickets, TicketStatus status) {
        boolean mismatch = tickets.stream().anyMatch(t -> t.getStatus() != status);
        if (mismatch) {
            throw new IllegalArgumentException("All tickets must be in status " + status);
        }
    }

    /**
     * Ensures the reservation for each ticket has not expired (30 min before show).
     */
    private void validateReservationStillValid(List<Ticket> tickets) {
        LocalDateTime now = LocalDateTime.now();
        for (Ticket t : tickets) {
            LocalDateTime cutoff = t.getShow().getDate().minusMinutes(30);
            if (now.isAfter(cutoff)) {
                throw new ReservationExpiredException("Reservation expired for ticket " + t.getId());
            }
        }
    }

    /**
     * Ensures it’s still at least 30 min before show to refund.
     */
    private void validateRefundWindow(List<Ticket> tickets) {
        LocalDateTime now = LocalDateTime.now();
        for (Ticket t : tickets) {
            LocalDateTime cutoff = t.getShow().getDate().minusMinutes(30);
            if (now.isAfter(cutoff)) {
                throw new IllegalArgumentException("Too late to refund ticket " + t.getId());
            }
        }
    }

    /**
     * Ensures the current user is the one who created/reserved/bought these tickets.
     */
    private void validateTicketsOwnedByCurrentUser(List<Ticket> tickets) {
        Long me = authenticationFacade.getCurrentUserId();
        boolean any = tickets.stream().anyMatch(t -> !t.getOrder().getUserId().equals(me));
        if (any) {
            throw new SeatUnavailableException("Cannot operate on tickets you do not own");
        }
    }


}
