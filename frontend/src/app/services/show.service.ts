import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { CreateShow } from '../dtos/create-show';
import { Show } from '../dtos/show';

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
}
