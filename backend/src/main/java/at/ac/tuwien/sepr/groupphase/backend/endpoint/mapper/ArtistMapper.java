package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ArtistDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CreateArtistDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Artist;
import at.ac.tuwien.sepr.groupphase.backend.entity.Show;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ArtistMapper {
    @Mapping(target = "showIds", source = "shows", qualifiedByName = "mapShowsToIds")
    ArtistDetailDto artistToArtistDetailDto(Artist artist);

    @Mapping(target = "shows", source = "showIds", qualifiedByName = "mapIdsToShows")
    Artist createArtistDtoToArtist(CreateArtistDto createArtistDto);

    List<ArtistDetailDto> artistsToArtistDetailDtos(List<Artist> artists);

    @Named("mapShowsToIds")
    default Set<Long> mapShowsToIds(Set<Show> shows) {
        if (shows == null) {
            return null;
        }
        return shows.stream()
            .map(Show::getId)
            .collect(Collectors.toSet());
    }

    @Named("mapIdsToShows")
    default Set<Show> mapIdsToShows(Set<Long> showIds) {
        if (showIds == null) {
            return null;
        }
        return showIds.stream()
            .map(id -> {
                Show show = new Show();
                show.setId(id);
                return show;
            })
            .collect(Collectors.toSet());
    }
}
