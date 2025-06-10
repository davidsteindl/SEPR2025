package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;

@Entity
public class StageSector extends Sector {
    @Override
    public boolean isBookable() {
        return false;
    }
}
