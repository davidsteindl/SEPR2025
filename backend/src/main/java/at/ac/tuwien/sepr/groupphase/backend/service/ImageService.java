package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Image;

public interface ImageService {

    /**
     * Find a single image by id.
     *
     * @param id the id of the image
     * @return the image entity
     */
    Image findById(Long id);

}
