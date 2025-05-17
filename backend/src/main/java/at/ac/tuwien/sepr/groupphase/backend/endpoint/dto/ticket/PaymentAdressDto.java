package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ticket;

import java.util.Objects;

public class PaymentAdressDto {
    private String city;
    private String street;
    private String postalCode;
    private String housenumber;
    private String country;
    private String uid;

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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentAdressDto that)) {
            return false;
        }
        return Objects.equals(city, that.city)
            && Objects.equals(street, that.street)
            && Objects.equals(postalCode, that.postalCode)
            && Objects.equals(housenumber, that.housenumber)
            && Objects.equals(country, that.country)
            && Objects.equals(uid, that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, street, postalCode, housenumber, country, uid);
    }

    @Override
    public String toString() {
        return "PaymentAdressDto{" +
            "city='" + city + '\'' +
            ", street='" + street + '\'' +
            ", postalCode='" + postalCode + '\'' +
            ", housenumber='" + housenumber + '\'' +
            ", country='" + country + '\'' +
            ", uid='" + uid + '\'' +
            '}';
    }

    public static final class PaymentAdressDtoBuilder {
        private String city;
        private String street;
        private String postalCode;
        private String housenumber;
        private String country;
        private String uid;

        private PaymentAdressDtoBuilder() {
        }

        public static PaymentAdressDtoBuilder aPaymentAdressDto() {
            return new PaymentAdressDtoBuilder();
        }

        public PaymentAdressDtoBuilder withCity(String city) {
            this.city = city;
            return this;
        }

        public PaymentAdressDtoBuilder withStreet(String street) {
            this.street = street;
            return this;
        }

        public PaymentAdressDtoBuilder withPostalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public PaymentAdressDtoBuilder withHousenumber(String housenumber) {
            this.housenumber = housenumber;
            return this;
        }

        public PaymentAdressDtoBuilder withCountry(String country) {
            this.country = country;
            return this;
        }

        public PaymentAdressDtoBuilder withUid(String uid) {
            this.uid = uid;
            return this;
        }

        public PaymentAdressDto build() {
            PaymentAdressDto dto = new PaymentAdressDto();
            dto.setCity(city);
            dto.setStreet(street);
            dto.setPostalCode(postalCode);
            dto.setHousenumber(housenumber);
            dto.setCountry(country);
            dto.setUid(uid);
            return dto;
        }
    }
}
