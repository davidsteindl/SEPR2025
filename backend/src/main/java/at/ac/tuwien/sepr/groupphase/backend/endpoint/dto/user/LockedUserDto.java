package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

public class LockedUserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isLocked;

    public LockedUserDto() {}

    public LockedUserDto(Long id, String firstName, String lastName, String email, Boolean isLocked) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.isLocked = isLocked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean locked) {
        isLocked = locked;
    }

    // Builder
    public static final class LockedUserDtoBuilder {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private Boolean isLocked;

        private LockedUserDtoBuilder() {}

        public static LockedUserDtoBuilder aLockedUserDto() {
            return new LockedUserDtoBuilder();
        }

        public LockedUserDtoBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public LockedUserDtoBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public LockedUserDtoBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public LockedUserDtoBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public LockedUserDtoBuilder withIsLocked(Boolean isLocked) {
            this.isLocked = isLocked;
            return this;
        }


        public LockedUserDto build() {
            return new LockedUserDto(id, firstName, lastName, email, isLocked);
        }
    }
}
