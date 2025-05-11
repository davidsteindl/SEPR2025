package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchResultDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LocationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PerformanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PerformanceSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShowRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.SearchService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.SearchValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.lang.invoke.MethodHandles;

import java.util.List;
import java.util.stream.Collectors;

public class CustomSearchService implements SearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EventRepository eventRepo;
    private final ShowRepository showRepo;
    private final SearchValidator searchValidator;

    public CustomSearchService(EventRepository eventRepo, ShowRepository showRepo, SearchValidator searchValidator) {
        this.eventRepo = eventRepo;
        this.showRepo = showRepo;
        this.searchValidator = searchValidator;
    }

    @Override
    public List<ArtistDto> searchArtists(ArtistSearchDto criteria) {
        return List.of();
    }

    @Override
    public List<LocationDto> searchLocations(LocationSearchDto criteria) {
        return List.of();
    }

    @Override
    public Page<EventSearchResultDto> searchEvents(EventSearchDto eventSearchDto) {
        LOGGER.debug("Search events with criteria: {}");

        searchValidator.validateForEvents(eventSearchDto);

        Specification<Event> spec = (root, query, cb) -> cb.conjunction();
        spec = spec
            .and(EventSpecifications.hasName(eventSearchDto.getName()))
            .and(EventSpecifications.hasType(eventSearchDto.getType()))
            .and(EventSpecifications.hasDescription(eventSearchDto.getDescription()));

        if (eventSearchDto.getDuration() != null) {
            spec = spec.and(EventSpecifications.hasDurationBetween(eventSearchDto.getDuration()));
        }

        Page<Event> page = eventRepo.findAll(spec,
            PageRequest.of(eventSearchDto.getPage(), eventSearchDto.getSize()));

        List<EventSearchResultDto> dtos = page.getContent().stream().map(e -> EventSearchResultDto.EventSearchResultDtoBuilder.anEventSearchResultDto()
                    .id(e.getId())
                    .name(e.getName())
                    .category(e.getCategory().getDisplayName())
                    .locationId(e.getLocation().getId())
                    .duration(e.getTotalDuration())
                    .description(e.getDescription())
                    .build()).collect(Collectors.toList());

        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }


    @Override
    public List<PerformanceDto> searchPerformances(PerformanceSearchDto criteria) {
        return List.of();
    }
}
