package at.ac.tuwien.sepr.groupphase.backend.service.validators;

import at.ac.tuwien.sepr.groupphase.backend.config.type.SectorType;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.roomdtos.SectorDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SectorValidator {

    private final SectorRepository sectorRepository;

    @Autowired
    public SectorValidator(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    public void validateSector(SectorDto sectorDto) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (sectorDto.getId() != null) {
            sectorRepository.findById(sectorDto.getId())
                .orElseThrow(() -> new ValidationException(
                    "Sector with ID " + sectorDto.getId() + " does not exist.",
                    List.of("Sector with ID " + sectorDto.getId() + " does not exist.")
                ));
        }

        String name = sectorDto.getName();
        if (name == null || name.trim().isEmpty()) {
            errors.add("Name must not be blank.");
        } else if (name.length() > 100) {
            errors.add("Name must not exceed 100 characters.");
        }

        SectorType type = sectorDto.getType();

        if (type == SectorType.STAGE) {
            if (sectorDto.getPrice() != null) {
                errors.add("Stage sectors cannot have a price.");
            }
        } else if (type == SectorType.STANDING || type == SectorType.NORMAL) {
            if (sectorDto.getPrice() == null || sectorDto.getPrice() <= 0) {
                errors.add("Sector price must be positive.");
            }
        } else {
            errors.add("Invalid sector type: " + type + ". Sector type must be NORMAL, STANDING or STAGE.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Sector validation failed.", errors);
        }
    }
}
