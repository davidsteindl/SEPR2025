import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { CreateShow } from '../dtos/create-show';
import {Show, ShowSearch, ShowSearchResult} from '../dtos/show';
import { forkJoin } from 'rxjs';
import {Page} from "../dtos/page";

@Injectable({
  providedIn: 'root'
})
export class ShowService {
  private showBaseUri: string = this.globals.backendUri + '/shows';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }


  /**
   * Creates a new show
   *
   * @param show to create
   */
  create(show: CreateShow): Observable<Show> {
    console.log('Create show with name: ' + show.name);
    return this.httpClient.post<Show>(this.showBaseUri, show);
  }

  /**
   * Retrieves all shows
   */
  getAll(): Observable<Show[]> {
    return this.httpClient.get<Show[]>(this.showBaseUri);
  }

  /**
   * Retrieves the show with the given ID
   *
   * @param id ID of the show to retrieve
   */
  getShowById(id: number): Observable<Show> {
    return this.httpClient.get<Show>(`${this.showBaseUri}/${id}`);
  }

  getShowsByIds(ids: number[]): Observable<Show[]> {
    return forkJoin(ids.map(id => this.getShowById(id)));
  }

  /**
   * Retrieves a paginated list of shows associated with a specific event.
   *
   * @param eventId - The ID of the event for which to retrieve shows.
   * @param page - The page number to retrieve (defaults to 0).
   * @param size - The number of items per page (defaults to 5).
   */
  getPagedShowsForEvent(eventId: number, page: number = 0, size: number = 5): Observable<Page<Show>> {
    return this.httpClient.get<Page<Show>>(
      `${this.globals.backendUri}/shows/event/${eventId}?page=${page}&size=${size}`
    );
  }

  searchShows(criteria: ShowSearch): Observable<Page<ShowSearchResult>> {
    return this.httpClient.post<Page<ShowSearchResult>>(
      `${this.showBaseUri}/search`,
      criteria
    );
  }
}
