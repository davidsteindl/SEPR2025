package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EventSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LocationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.LocationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PerformanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PerformanceSearchDto;

import java.util.List;

public class CustomSearchService implements at.ac.tuwien.sepr.groupphase.backend.service.SearchService {
    @Override
    public List<ArtistDto> searchArtists(ArtistSearchDto criteria) {
        return List.of();
    }

    @Override
    public List<LocationDto> searchLocations(LocationSearchDto criteria) {
        return List.of();
    }

    @Override
    public Page<EventDetailDto> searchEvents(EventSearchDto criteria) {
        return List.of();
    }

    @Override
    public List<PerformanceDto> searchPerformances(PerformanceSearchDto criteria) {
        return List.of();
    }
}
