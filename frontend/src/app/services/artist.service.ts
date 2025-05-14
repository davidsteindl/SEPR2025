import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Globals} from '../global/globals';
import {Artist, ArtistSearchDto, ArtistSearchResultDto} from "../dtos/artist";
import {Page} from "../dtos/page";

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
   * @return an Observable containing a page of matching artists
   */
  searchArtists(criteria: ArtistSearchDto): Observable<Page<ArtistSearchResultDto>> {
    return this.httpClient.post<Page<ArtistSearchResultDto>>(
      this.artistBaseUri + '/search',
      criteria
    );
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

