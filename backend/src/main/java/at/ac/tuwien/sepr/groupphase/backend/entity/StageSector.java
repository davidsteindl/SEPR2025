package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;

import java.util.Objects;

@Entity
public class StageSector extends Sector {

    @Override
    public boolean isBookable() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StageSector that)) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    public static final class StageSectorBuilder {
        private String name;
        private Room room;

        public static StageSectorBuilder aStageSector() {
            return new StageSectorBuilder();
        }

        public StageSectorBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public StageSectorBuilder withRoom(Room room) {
            this.room = room;
            return this;
        }

        public StageSector build() {
            StageSector stageSector = new StageSector();
            stageSector.setName(name);
            stageSector.setRoom(room);
            return stageSector;
        }
    }
}
