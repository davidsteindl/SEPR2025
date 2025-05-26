package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.EventLocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.show.ShowSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ArtistMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.EventLocation;
import at.ac.tuwien.sepr.groupphase.backend.entity.Sector;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventLocationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.SearchService;
import at.ac.tuwien.sepr.groupphase.backend.service.specifications.ArtistSpecifications;
import at.ac.tuwien.sepr.groupphase.backend.service.specifications.EventLocationSpecifications;
import at.ac.tuwien.sepr.groupphase.backend.service.specifications.EventSpecifications;
import at.ac.tuwien.sepr.groupphase.backend.service.specifications.ShowSpecifications;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.SearchValidator;
import java.util.Comparator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CustomSearchService implements SearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventRepository eventRepo;
    private final EventLocationRepository eventLocationRepo;
    private final ShowRepository showRepo;
    private final SearchValidator searchValidator;
    private final ArtistRepository artistRepo;
    private final ArtistMapper artistMapper;

    @Autowired
    public CustomSearchService(EventRepository eventRepo, ShowRepository showRepo, EventLocationRepository eventLocationRepo, SearchValidator searchValidator, ArtistRepository artistRepo,
                               ArtistMapper artistMapper) {
        this.eventRepo = eventRepo;
        this.showRepo = showRepo;
        this.eventLocationRepo = eventLocationRepo;
        this.searchValidator = searchValidator;
        this.artistRepo = artistRepo;
        this.artistMapper = artistMapper;
    }

    @Override
    public Page<ArtistSearchResultDto> searchArtists(ArtistSearchDto criteria) throws ValidationException {
        LOGGER.debug("Searching artists with criteria: {}", criteria);

        searchValidator.validateForArtists(criteria);

        Specification<Artist> spec = (root, query, cb) -> cb.conjunction();
        spec = spec
            .and(ArtistSpecifications.hasFirstnameLike(criteria.getFirstname()))
            .and(ArtistSpecifications.hasLastnameLike(criteria.getLastname()))
            .and(ArtistSpecifications.hasStagenameLike(criteria.getStagename()));

        Page<Artist> page = artistRepo.findAll(spec, PageRequest.of(criteria.getPage(), criteria.getSize()));

        List<ArtistSearchResultDto> dtos = page.getContent().stream()
            .map(artist -> ArtistSearchResultDto.ArtistSearchResultDtoBuilder.anArtistSearchResultDto()
                .id(artist.getId())
                .firstname(artist.getFirstname())
                .lastname(artist.getLastname())
                .stagename(artist.getStagename())
                .build())
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Override
    public Page<EventLocationDetailDto> searchEventLocations(EventLocationSearchDto eventLocationSearchDto) throws ValidationException {
        LOGGER.debug("Search eventlocations with criteria: {}", eventLocationSearchDto);

        searchValidator.validateForEventLocations(eventLocationSearchDto);

        Specification<EventLocation> spec = (root, query, cb) -> cb.conjunction();
        spec = spec
            .and(EventLocationSpecifications.hasNameLike(eventLocationSearchDto.getName()))
            .and(EventLocationSpecifications.hasStreetLike(eventLocationSearchDto.getStreet()))
            .and(EventLocationSpecifications.hasCityLike(eventLocationSearchDto.getCity()))
            .and(EventLocationSpecifications.hasCountryLike(eventLocationSearchDto.getCountry()))
            .and(EventLocationSpecifications.hasPostalCodeLike(eventLocationSearchDto.getPostalCode()));

        Page<EventLocation> page = eventLocationRepo.findAll(spec, PageRequest.of(eventLocationSearchDto.getPage(), eventLocationSearchDto.getSize()));

        List<EventLocationDetailDto> dtos = page.getContent().stream()
            .map(eventLocation -> EventLocationDetailDto.EventLocationDtoBuilder.anEventLocationDto()
                .id(eventLocation.getId())
                .name(eventLocation.getName())
                .street(eventLocation.getStreet())
                .city(eventLocation.getCity())
                .country(eventLocation.getCountry())
                .postalCode(eventLocation.getPostalCode())
                .build())
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Override
    public Page<EventSearchResultDto> searchEvents(EventSearchDto eventSearchDto) throws ValidationException {
        LOGGER.debug("Search events with criteria: {}", eventSearchDto);

        searchValidator.validateForEvents(eventSearchDto);

        Specification<Event> spec = (root, query, cb) -> cb.conjunction();
        spec = spec
            .and(EventSpecifications.hasName(eventSearchDto.getName()))
            .and(EventSpecifications.hasCategory(eventSearchDto.getCategory()))
            .and(EventSpecifications.hasDescription(eventSearchDto.getDescription()));

        if (eventSearchDto.getDuration() != null) {
            spec = spec.and(EventSpecifications.hasDurationBetween(eventSearchDto.getDuration()));
        }

        Page<Event> page = eventRepo.findAll(spec, PageRequest.of(eventSearchDto.getPage(), eventSearchDto.getSize()));

        List<EventSearchResultDto> dtos = page.getContent().stream().map(e -> EventSearchResultDto.EventSearchResultDtoBuilder.anEventSearchResultDto()
            .id(e.getId())
            .name(e.getName())
            .category(e.getCategory().getDisplayName())
            .locationId(e.getLocation().getId())
            .duration(e.getDuration())
            .description(e.getDescription())
            .build()).collect(Collectors.toList());

        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }


    @Override
    public Page<ShowSearchResultDto> searchShows(ShowSearchDto criteria) throws ValidationException {
        LOGGER.debug("Searching shows with criteria: {}", criteria);

        searchValidator.validateForShows(criteria);

        Specification<Show> spec = (root, query, cb) -> cb.conjunction();

        spec = spec
            .and(ShowSpecifications.dateBetween(criteria.getStartDate(), criteria.getEndDate()))
            .and(ShowSpecifications.hasEventName(criteria.getEventName()))
            .and(ShowSpecifications.hasRoomName(criteria.getRoomName()))
            .and(ShowSpecifications.nameContains(criteria.getName()))
            .and(ShowSpecifications.hasPriceBetween(criteria.getMinPrice(), criteria.getMaxPrice()));

        PageRequest pageable = PageRequest.of(criteria.getPage(), criteria.getSize());
        Page<Show> page = showRepo.findAll(spec, pageable);

        List<ShowSearchResultDto> result = page.getContent().stream().map(show -> {
            ShowSearchResultDto dto = new ShowSearchResultDto();
            dto.setId(show.getId());
            dto.setName(show.getName());
            dto.setDuration(show.getDuration());
            dto.setDate(show.getDate());
            dto.setEventId(show.getEvent().getId());
            dto.setEventName(show.getEvent().getName());
            dto.setRoomId(show.getRoom().getId());
            dto.setRoomName(show.getRoom().getName());

            var sectorPrices = show.getRoom().getSectors().stream()
                .map(sector -> BigDecimal.valueOf(sector.getPrice()))
                .toList();

            BigDecimal min = sectorPrices.stream()
                .min(Comparator.naturalOrder())
                .orElse(null);

            BigDecimal max = sectorPrices.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);


            dto.setMinPrice(min);
            dto.setMaxPrice(max);
            return dto;
        }).collect(Collectors.toList());

        return new PageImpl<>(result, pageable, page.getTotalElements());
    }
}

