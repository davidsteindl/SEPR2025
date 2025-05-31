package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("STAGE")
public class StageSectorDto extends SectorDto {

    public StageSectorDto() {
        super();
        setType(SectorType.STAGE);
    }
}
