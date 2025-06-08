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
    private List<Long> reservedTicketIds;

    private String cardNumber;
    private String expirationDate;
    private String securityCode;

    private String firstName;
    private String lastName;
    private String housenumber;
    private String country;
    private String city;
    private String street;
    private String postalCode;

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

    public List<Long> getReservedTicketIds() {
        return reservedTicketIds;
    }

    public void setReservedTicketIds(List<Long> reservedTicketIds) {
        this.reservedTicketIds = reservedTicketIds;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getHousenumber() {
        return housenumber;
    }

    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
