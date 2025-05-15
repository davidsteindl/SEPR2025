import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { Globals } from '../global/globals';
import { CreateEvent } from '../dtos/create-event';
import { Event } from '../dtos/event';
import {Page} from "../dtos/page";

@Injectable({
  providedIn: 'root'
})
export class EventService {

  private eventBaseUri: string = this.globals.backendUri + '/events';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Creates a new event
   *
   * @param event to create
   */
  create(event: CreateEvent): Observable<Event> {
    console.log('Create event with name: ' + event.name);
    return this.httpClient.post<Event>(this.eventBaseUri, {
      name: event.name,
      category: event.category,
      description: event.description,
      duration: event.duration,
      locationId: event.locationId
    });
  }

  /**
   * Retrieves all events
   */
  getAll(): Observable<Event[]> {
    return this.httpClient.get<Event[]>(this.eventBaseUri);
  }

  /**
   * Retrieves the event with the given ID
   *
   * @param id ID of the event to retrieve
   */
  getEventById(id: number): Observable<Event> {
    return this.httpClient.get<Event>(`${this.eventBaseUri}/${id}`);
  }

  getEventsByIds(ids: number[]): Observable<Event[]> {
    return forkJoin(ids.map(id => this.getEventById(id)));
  }


  /**
   * Retrieves all events linked to a specific artist via shows, paginated.
   *
   * @param artistId ID of the artist whose events should be fetched
   * @param page Page number for pagination (default: 0)
   * @param size Number of items per page (default: 5)
   * @returns An Observable of a paginated list of Event objects
   */
  getEventsByArtist(artistId: number, page = 0, size = 5): Observable<Page<Event>> {
    const url = `${this.eventBaseUri}/by-artist/${artistId}?page=${page}&size=${size}`;
    return this.httpClient.get<Page<Event>>(url);
  }
}
