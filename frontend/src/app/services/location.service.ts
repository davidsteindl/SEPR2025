import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Globals } from '../global/globals';
import { Observable } from 'rxjs';
import { Location } from '../dtos/location';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  private locationBaseUri: string = this.globals.backendUri + '/locations';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Retrieves all locations
   */
  getAll(): Observable<Location[]> {
    return this.httpClient.get<Location[]>(this.locationBaseUri);
  }

  /**
   * Retrieves the location with the given ID from the backend
   *
   * @param id to find
   */
  getLocationById(id: number): Observable<Location> {
    return this.httpClient.get<Location>(`${this.locationBaseUri}/${id}`);
  }
}
