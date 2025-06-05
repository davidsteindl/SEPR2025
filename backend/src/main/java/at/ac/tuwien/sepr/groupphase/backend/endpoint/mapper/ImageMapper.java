package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.entity.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    ImageMapper INSTANCE = Mappers.getMapper(ImageMapper.class);

    // Mapping für eine Liste von MultipartFile zu einer Liste von Images
    List<Image> toImageList(List<MultipartFile> multipartFiles);

    // Einzelnes MultipartFile zu Image mappen
    @Mapping(target = "id", ignore = true) // ID wird von der Datenbank generiert
    @Mapping(target = "image", expression = "java(file.getBytes())")
    @Mapping(target = "imageType", expression = "java(file.getContentType())")
    Image toImage(MultipartFile file) throws IOException;
}
