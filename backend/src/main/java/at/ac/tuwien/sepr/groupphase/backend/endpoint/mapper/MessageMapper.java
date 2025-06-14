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

    /**
     * This is necessary since the SimpleMessageDto misses the text property and the collection mapper can't handle
     * missing fields.
     **/
    @IterableMapping(qualifiedByName = "simpleMessage")
    List<SimpleMessageDto> messageToSimpleMessageDto(List<Message> message);

    DetailedMessageDto messageToDetailedMessageDto(Message message);

    Message detailedMessageDtoToMessage(DetailedMessageDto detailedMessageDto);

    Message messageInquiryDtoToMessage(MessageInquiryDto messageInquiryDto);

    MessageInquiryDto messageToMessageInquiryDto(Message message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "images", source = "multipartFiles") // Mappe MultipartFiles zu Images
    Message toMessage(MessageInquiryDto messageDto, List<MultipartFile> multipartFiles);


}