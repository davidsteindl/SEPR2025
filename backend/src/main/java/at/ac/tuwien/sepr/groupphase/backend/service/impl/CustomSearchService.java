package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.artist.ArtistSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.event.EventSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.LocationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.eventlocation.LocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.performance.PerformanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.performance.PerformanceSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ArtistMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ArtistRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.SearchService;
import at.ac.tuwien.sepr.groupphase.backend.service.specifications.ArtistSpecifications;
import at.ac.tuwien.sepr.groupphase.backend.service.specifications.EventSpecifications;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.SearchValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomSearchService implements SearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventRepository eventRepo;
    private final ShowRepository showRepo;
    private final SearchValidator searchValidator;
    private final ArtistRepository artistRepo;
    private final ArtistMapper artistMapper;

    @Autowired
    public CustomSearchService(EventRepository eventRepo, ShowRepository showRepo, SearchValidator searchValidator, ArtistRepository artistRepo,
                               ArtistMapper artistMapper) {
        this.eventRepo = eventRepo;
        this.showRepo = showRepo;
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
    public List<LocationDto> searchLocations(LocationSearchDto criteria) {
        return List.of();
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
    public List<PerformanceDto> searchPerformances(PerformanceSearchDto criteria) {
        return List.of();
    }
}

