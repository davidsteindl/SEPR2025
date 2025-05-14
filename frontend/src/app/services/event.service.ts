import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { Globals } from '../global/globals';
import { CreateEvent } from '../dtos/create-event';
import { Event } from '../dtos/event';

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
}
