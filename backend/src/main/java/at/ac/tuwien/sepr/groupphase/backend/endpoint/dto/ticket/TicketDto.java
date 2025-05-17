package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import java.util.Objects;

public class TicketDto {
    private Long id;
    private String showName;
    private int price;
    private Long sectorId;
    private int rowNumber;
    private int columnNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TicketDto that)) {
            return false;
        }
        return price == that.price
            && rowNumber == that.rowNumber
            && columnNumber == that.columnNumber
            && Objects.equals(id, that.id)
            && Objects.equals(showName, that.showName)
            && Objects.equals(sectorId, that.sectorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, showName, price, sectorId, rowNumber, columnNumber);
    }

    @Override
    public String toString() {
        return "TicketDto{"
            + "id=" + id
            + ", showName='" + showName + '\''
            + ", price=" + price
            + ", sectorId=" + sectorId
            + ", rowNumber=" + rowNumber
            + ", columnNumber=" + columnNumber
            + '}';
    }

    public static final class TicketDtoBuilder {
        private Long id;
        private String showName;
        private int price;
        private Long sectorId;
        private int rowNumber;
        private int columnNumber;

        private TicketDtoBuilder() {
        }

        public static TicketDtoBuilder aTicketDto() {
            return new TicketDtoBuilder();
        }

        public TicketDtoBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TicketDtoBuilder withShowName(String showName) {
            this.showName = showName;
            return this;
        }

        public TicketDtoBuilder withPrice(int price) {
            this.price = price;
            return this;
        }

        public TicketDtoBuilder withSectorId(Long sectorId) {
            this.sectorId = sectorId;
            return this;
        }

        public TicketDtoBuilder withRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
            return this;
        }

        public TicketDtoBuilder withColumnNumber(int columnNumber) {
            this.columnNumber = columnNumber;
            return this;
        }

        public TicketDto build() {
            TicketDto dto = new TicketDto();
            dto.setId(id);
            dto.setShowName(showName);
            dto.setPrice(price);
            dto.setSectorId(sectorId);
            dto.setRowNumber(rowNumber);
            dto.setColumnNumber(columnNumber);
            return dto;
        }
    }
}
