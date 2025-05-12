import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Globals} from '../global/globals';
import {Artist, ArtistDto, ArtistSearchDto} from "../dtos/artist";

@Injectable({
  providedIn: 'root'
})
export class ArtistService {
  private artistBaseUri: string = this.globals.backendUri + '/artists';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Searches for artists that match the given search criteria.
   *
   * @param criteria the search term (can match firstname, lastname or stagename)
   * @return an Observable containing a list of matching artists
   */
  searchArtists(criteria: ArtistSearchDto): Observable<ArtistDto[]> {
    return this.httpClient.post<ArtistDto[]>(this.artistBaseUri + '/search', criteria);
  }


  /**
   * Retrieve the artist with the given ID from the backend.
   *
   * @param id the unique identifier of the artist to retrieve
   * @return an Observable containing the artist data
   */
  getArtistById(id: number): Observable<Artist> {
    return this.httpClient.get<Artist>(this.artistBaseUri + "/" +id);
  }
}

