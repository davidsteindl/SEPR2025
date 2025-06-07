package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.message.ImageDto;
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

    List<Image> toImageList(List<MultipartFile> multipartFiles);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "image", expression = "java(file.getBytes())")
    @Mapping(target = "imageType", expression = "java(file.getContentType())")
    Image toImage(MultipartFile file) throws IOException;

    ImageDto toImageDto(Image image);
}
