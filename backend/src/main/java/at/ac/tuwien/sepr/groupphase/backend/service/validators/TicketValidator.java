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
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HoldRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ticket.TicketRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.AuthenticationFacade;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.RoomServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ShowServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        if (seatId != null) {
            // Seated: any valid hold on this seat blocks it
            boolean conflict = holds.stream()
                .anyMatch(h -> h.getValidUntil().isAfter(now)
                    && h.getSectorId().equals(sectorId)
                    && h.getSeatId() != null && h.getSeatId().equals(seatId));
            if (conflict) {
                throw new SeatUnavailableException("Seat already on hold for show " + showId);
            }
        } else {
            // Standing: only block if holds >= capacity
            long activeHolds = holds.stream()
                .filter(h -> h.getValidUntil().isAfter(now)
                    && h.getSectorId().equals(sectorId)
                    && h.getSeatId() == null)
                .count();
            long capacity = ((StandingSector) roomService.getSectorById(sectorId)).getCapacity();
            if (activeHolds >= capacity) {
                throw new SeatUnavailableException("Standing sector already fully on hold for show " + showId);
            }
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

        boolean any = tickets.stream()
            .flatMap(t -> t.getOrders().stream())
            .anyMatch(o -> !o.getUserId().equals(me));

        if (any) {
            throw new SeatUnavailableException("Cannot operate on tickets you do not own");
        }
    }

    /**
     * Validates credit card fields in the checkout request, including number format,
     * expiration date, and CVC. Throws ValidationException if any errors are found.
     *
     * @param dto the checkout request containing payment details
     * @throws ValidationException if any credit card field is invalid
     */
    public void validateCheckoutPaymentData(TicketRequestDto dto) throws ValidationException {
        LOGGER.debug("Validating credit card data with bulk validation");

        List<String> errors = new ArrayList<>();

        if (dto.getCardNumber() == null || !dto.getCardNumber().matches("\\d{13,19}")) {
            errors.add("Invalid credit card number format");
        } else if (!passesLuhnCheck(dto.getCardNumber())) {
            errors.add("Invalid credit card number (Luhn check failed)");
        }

        if (dto.getExpirationDate() == null || !dto.getExpirationDate().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            errors.add("Invalid expiration date format (must be MM/YY)");
        } else {
            try {
                String[] parts = dto.getExpirationDate().split("/");
                int expMonth = Integer.parseInt(parts[0]);
                int expYear = 2000 + Integer.parseInt(parts[1]);

                LocalDateTime now = LocalDateTime.now();
                if (expYear < now.getYear() || (expYear == now.getYear() && expMonth < now.getMonthValue())) {
                    errors.add("Credit card is expired");
                }
            } catch (Exception e) {
                errors.add("Unable to parse expiration date");
            }
        }

        if (dto.getSecurityCode() == null || !dto.getSecurityCode().matches("\\d{3,4}")) {
            errors.add("Invalid CVC code (must be 3 or 4 digits)");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Invalid credit card data", errors);
        }
    }

    /**
     * Performs a Luhn algorithm check on a given credit card number to verify its validity.
     *
     * @param number the credit card number as a string
     * @return true if the number passes the Luhn check, false otherwise
     */
    private boolean passesLuhnCheck(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    /**
     * Validates personal and address-related fields in the checkout request.
     * Ensures all required fields are present and meet basic format and length constraints.
     *
     * @param dto the checkout request containing address and personal data
     * @throws ValidationException if any address or name field is invalid or incomplete
     */
    public void validateCheckoutAddress(TicketRequestDto dto) throws ValidationException {
        List<String> validationErrors = new ArrayList<>();

        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) {
            validationErrors.add("First name is required");
        } else if (dto.getFirstName().length() > 100) {
            validationErrors.add("First name is too long (max 100 characters)");
        }

        if (dto.getLastName() == null || dto.getLastName().isBlank()) {
            validationErrors.add("Last name is required");
        } else if (dto.getLastName().length() > 100) {
            validationErrors.add("Last name is too long (max 100 characters)");
        }

        int addressCounter = 0;

        if (dto.getStreet() != null) {
            if (dto.getStreet().isBlank()) {
                validationErrors.add("Street is given but blank");
            } else {
                addressCounter++;
            }
            if (dto.getStreet().length() > 200) {
                validationErrors.add("Street is too long (max 200 characters)");
            }
        }

        if (dto.getHousenumber() != null) {
            if (dto.getHousenumber().isBlank()) {
                validationErrors.add("House number is given but blank");
            } else {
                addressCounter++;
            }
            if (dto.getHousenumber().length() > 100) {
                validationErrors.add("House number is too long (max 100 characters)");
            }
        }

        if (dto.getPostalCode() != null) {
            if (dto.getPostalCode().isBlank()) {
                validationErrors.add("Postal code is given but blank");
            } else {
                addressCounter++;
            }
            if (dto.getPostalCode().length() > 20) {
                validationErrors.add("Postal code is too long (max 20 characters)");
            }
        }

        if (dto.getCity() != null) {
            if (dto.getCity().isBlank()) {
                validationErrors.add("City is given but blank");
            } else {
                addressCounter++;
            }
            if (dto.getCity().length() > 100) {
                validationErrors.add("City is too long (max 100 characters)");
            }
        }

        if (dto.getCountry() != null) {
            if (dto.getCountry().isBlank()) {
                validationErrors.add("Country is given but blank");
            } else {
                addressCounter++;
            }
            if (dto.getCountry().length() > 100) {
                validationErrors.add("Country is too long (max 100 characters)");
            }
        }

        if (addressCounter != 0 && addressCounter < 5) {
            validationErrors.add("Address is incomplete: please fill out all address fields");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Checkout address validation failed", validationErrors);
        }
    }

}
