import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {CreateArtist} from '../dtos/create-artist';
import {Artist} from '../dtos/artist';

@Injectable({
  providedIn: 'root'
})
export class ArtistService {
  private artistBaseUri: string = this.globals.backendUri + '/artists';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }


  /**
   * Creates a new artist
   *
   * @param artist to create
   */
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

  /**
   * Retrieves all artists
   */
  getAll(): Observable<Artist[]> {
    return this.httpClient.get<Artist[]>(this.artistBaseUri);
  }

  /**
   * Retrieves the artist with the given ID
   *
   * @param id ID of the artist to retrieve
   */
  getShowById(id: number): Observable<Artist> {
    return this.httpClient.get<Artist>(`${this.artistBaseUri}/${id}`);
  }
}
