package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.message.DetailedMessageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.message.MessageInquiryDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.message.SimpleMessageDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Message;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface MessageMapper {

    @Named("simpleMessage")
    SimpleMessageDto messageToSimpleMessageDto(Message message);

    @IterableMapping(qualifiedByName = "simpleMessage")
    List<SimpleMessageDto> messageToSimpleMessageDto(List<Message> message);

    DetailedMessageDto messageToDetailedMessageDto(Message message);

    Message detailedMessageDtoToMessage(DetailedMessageDto detailedMessageDto);

    @Mapping(target = "images", expression = "java(new java.util.ArrayList<>())")
    Message messageInquiryDtoToMessage(MessageInquiryDto messageInquiryDto);

    MessageInquiryDto messageToMessageInquiryDto(Message message);

    @Mapping(target = "id", ignore = true)
    @Mapping(
        target = "images",
        expression = "java(multipartFiles != null ? imageMapper.toImageList(multipartFiles) : new java.util.ArrayList<>())"
    )
    Message toMessage(MessageInquiryDto messageDto, List<MultipartFile> multipartFiles);
}