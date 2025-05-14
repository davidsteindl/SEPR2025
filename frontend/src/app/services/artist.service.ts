import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { Artist, ArtistSearchDto, ArtistSearchResultDto } from '../dtos/artist';
import { CreateArtist } from '../dtos/create-artist';
import { Page } from '../dtos/page';

@Injectable({
  providedIn: 'root'
})
export class ArtistService {
  private artistBaseUri: string = this.globals.backendUri + '/artists';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  create(artist: CreateArtist): Observable<Artist> {
    let name = '';
    if (artist.stagename) {
      name = artist.stagename;
    } else if (artist.firstname && artist.lastname) {
      name = artist.firstname + ' ' + artist.lastname;
    }
    console.log('Create artist with name: ' + name);
    return this.httpClient.post<Artist>(this.artistBaseUri, artist);
  }

  getAll(): Observable<Artist[]> {
    return this.httpClient.get<Artist[]>(this.artistBaseUri);
  }

  getShowById(id: number): Observable<Artist> {
    return this.httpClient.get<Artist>(`${this.artistBaseUri}/${id}`);
  }

  searchArtists(criteria: ArtistSearchDto): Observable<Page<ArtistSearchResultDto>> {
    return this.httpClient.post<Page<ArtistSearchResultDto>>(
      this.artistBaseUri + '/search',
      criteria
    );
  }

  getArtistById(id: number): Observable<Artist> {
    return this.httpClient.get<Artist>(`${this.artistBaseUri}/${id}`);
  }
}
