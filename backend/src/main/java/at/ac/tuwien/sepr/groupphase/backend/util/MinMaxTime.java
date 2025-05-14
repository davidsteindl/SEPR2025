package at.ac.tuwien.sepr.groupphase.backend.util;

import java.sql.Timestamp;

public interface MinMaxTime {
    Timestamp getMinDate();

    Timestamp getMaxEnd();
}
