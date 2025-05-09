package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreateShowDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShowDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Event;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ShowMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "artistIds", source = "artists", qualifiedByName = "mapArtistsToIds")
    ShowDetailDto showToShowDetailDto(Show show);

    @Mapping(target = "event", source = "eventId", qualifiedByName = "mapEventIdToEvent")
    @Mapping(target = "artists", source = "artistIds", qualifiedByName = "mapIdsToArtists")
    Show createShowDtoToShow(CreateShowDto createShowDto);

    List<ShowDetailDto> showsToShowDetailDtos(List<Show> shows);

    @Named("mapEventIdToEvent")
    default Event mapEventIdToEvent(Long eventId) {
        if (eventId == null) {
            throw new ValidationException("Event ID must not be null");
        }
        Event event = new Event();
        event.setId(eventId);
        return event;
    }

    @Named("mapArtistsToIds")
    default Set<Long> mapArtistsToIds(Set<Artist> artists) {
        if (artists == null) {
            return null;
        }
        return artists.stream()
            .map(Artist::getId)
            .collect(Collectors.toSet());
    }

    @Named("mapIdsToArtists")
    default Set<Artist> mapIdsToArtists(Set<Long> artistIds) {
        if (artistIds == null) {
            throw new ValidationException("Artist IDs must not be null");
        }
        return artistIds.stream()
            .map(id -> {
                Artist artist = new Artist();
                artist.setId(id);
                return artist;
            })
            .collect(Collectors.toSet());
    }
}
