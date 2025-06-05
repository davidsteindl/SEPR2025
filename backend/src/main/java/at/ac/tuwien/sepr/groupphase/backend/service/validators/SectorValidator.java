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

        if (sectorDto.getType() != null) {
            if (!sectorDto.getType().equals(SectorType.STANDING) || !sectorDto.getType().equals(SectorType.STAGE)) {
                throw new ValidationException("Invalid sector type: " + sectorDto.getType(),
                    List.of("Sector type must be either null, STANDING or STAGE."));

            }
        }

        if (sectorDto.getType().equals(SectorType.STAGE)) {
            if (sectorDto.getPrice() != null) {
                throw new ValidationException("Stage sectors cannot have a price.", List.of("Stage sectors cannot have a price."));
            }
        } else if (sectorDto.getPrice() <= 0) {
            throw new ValidationException("Sector price must be positive.", List.of("Sector price must be positive."));
        }
    }
}
