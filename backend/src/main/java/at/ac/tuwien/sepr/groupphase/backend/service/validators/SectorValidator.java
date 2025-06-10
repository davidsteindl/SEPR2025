package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SectorValidator {

    private final SectorRepository sectorRepository;

    @Autowired
    public SectorValidator(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    public void validateSector(SectorDto sectorDto) throws ValidationException {
        if (sectorDto.getId() != null) {
            sectorRepository.findById(sectorDto.getId())
                .orElseThrow(() -> new ValidationException("Sector with ID " + sectorDto.getId() + " does not exist.",
                    List.of("Sector with ID " + sectorDto.getId() + " does not exist.")));
        }

        SectorType type = sectorDto.getType();

        if (type == SectorType.STAGE) {
            if (sectorDto.getPrice() != null) {
                throw new ValidationException("Stage sectors cannot have a price.",
                    List.of("Stage sectors cannot have a price."));
            }

        } else if (type == SectorType.STANDING || type == SectorType.NORMAL) {
            if (sectorDto.getPrice() == null || sectorDto.getPrice() <= 0) {
                throw new ValidationException("Sector price must be positive.",
                    List.of("Sector price must be positive."));
            }

        } else {
            throw new ValidationException("Invalid sector type: " + type,
                List.of("Sector type must be NORMAL, STANDING or STAGE."));
        }
    }
}
