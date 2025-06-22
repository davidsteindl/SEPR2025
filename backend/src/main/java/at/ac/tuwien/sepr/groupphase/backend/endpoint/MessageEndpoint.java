package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.message.DetailedMessageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.message.MessageInquiryDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.message.SimpleMessageDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.MessageMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.ImageService;
import at.ac.tuwien.sepr.groupphase.backend.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api/v1/news")
public class MessageEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final MessageService messageService;
    private final MessageMapper messageMapper;
    private final ImageService imageService;

    @Autowired
    public MessageEndpoint(MessageService messageService, MessageMapper messageMapper, ImageService imageService) {
        this.messageService = messageService;
        this.messageMapper = messageMapper;
        this.imageService = imageService;
    }

    @Secured("ROLE_USER")
    @GetMapping
    @Operation(summary = "Get list of messages without details", security = @SecurityRequirement(name = "apiKey"))
    public List<SimpleMessageDto> findAll() {
        LOGGER.info("GET /api/v1/news");
        return messageMapper.messageToSimpleMessageDto(messageService.findAll());
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/paginated")
    @Operation(summary = "Get list of messages without details", security = @SecurityRequirement(name = "apiKey"))
    public Page<SimpleMessageDto> findAllPaginated(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        LOGGER.info("GET /api/v1/news");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("publishedAt")));
        return messageService.findAllPaginated(pageable)
            .map(messageMapper::messageToSimpleMessageDto);
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/{id}")
    @Operation(summary = "Get detailed information about a specific message", security = @SecurityRequirement(name = "apiKey"))
    public DetailedMessageDto find(@PathVariable(name = "id") Long id) {
        LOGGER.info("GET /api/v1/news/{}", id);
        return messageMapper.messageToDetailedMessageDto(messageService.findOneWithImage(id));
    }

    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Publish a new message", security = @SecurityRequirement(name = "apiKey"))
    public DetailedMessageDto create(@RequestPart(value = "message") @Valid MessageInquiryDto messageDto,
                                     @RequestPart(value = "images", required = false) List<MultipartFile> files) {

        LOGGER.info("POST /api/v1/news body: {}", messageDto);
        return messageMapper.messageToDetailedMessageDto(
            messageService.publishMessage(messageMapper.toMessage(messageDto, files)));
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/{id}/image/{imageId}")
    @Operation(summary = "Get a specific image", security = @SecurityRequirement(name = "apiKey"))
    public ResponseEntity<byte[]> findImage(@PathVariable(name = "id") Long id, @PathVariable(name = "imageId") Long imageId) {
        LOGGER.info("GET /api/v1/news/{}/image/{}", id, imageId);
        var image = imageService.findById(imageId);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.getImageType()))
            .body(image.getImage());
    }
}
