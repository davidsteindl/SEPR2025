import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Globals} from '../global/globals';
import {map, Observable} from 'rxjs';
import {Location} from '../dtos/location';
import {CreateLocation} from "../dtos/create-location";

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  private locationBaseUri: string = this.globals.backendUri + '/locations';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Creates a new location
   *
   * @param location to create
   */
  create(location: CreateLocation): Observable<Location> {
    console.log('Create location with name: ' + location.name);
    return this.httpClient.post<Location>(this.locationBaseUri, {
      name: location.name,
      type: location.type,
      country: location.country,
      city: location.city,
      street: location.street,
      postalCode: location.postalCode
    });
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

  getCountries(): Observable<string[]> {
    return this.httpClient.get('assets/countrynames.csv', {responseType: 'text'}).pipe(
      map((data) => {
        return data.split('\n').map(line => line.trim()).filter(line => line.length > 0);
      })
    );
  }
}
