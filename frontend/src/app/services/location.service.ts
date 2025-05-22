import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Globals } from '../global/globals';
import { map, Observable } from 'rxjs';
import { EventLocationSearchDto, Location } from '../dtos/location';
import { CreateLocation } from "../dtos/create-location";
import { Page } from "../dtos/page";
import { ShowSearchResult } from "../dtos/show";

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  private locationBaseUri: string = this.globals.backendUri + '/locations';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Creates a new location
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
   */
  getLocationById(id: number): Observable<Location> {
    return this.httpClient.get<Location>(`${this.locationBaseUri}/${id}`);
  }

  /**
   * Retrieves the list of countries from a CSV file
   */
  getCountries(): Observable<string[]> {
    return this.httpClient.get('assets/countrynames.csv', { responseType: 'text' }).pipe(
      map((data) => {
        return data.split('\n').map(line => line.trim()).filter(line => line.length > 0);
      })
    );
  }

  /**
   * Retrieves all eventlocations specified by the search criteria, paginated.
   */
  searchEventLocations(criteria: EventLocationSearchDto): Observable<Page<Location>> {
    return this.httpClient.post<Page<Location>>(
      this.locationBaseUri + '/search',
      criteria
    );
  }

  /**
   * Retrieves all events for the given location ID
   */
  getShowsForEventLocation(locationId: number, page: number, size: number): Observable<Page<ShowSearchResult>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.httpClient.get<Page<ShowSearchResult>>(
      `${this.locationBaseUri}/${locationId}/shows/paginated`,
      { params }
    );
  }
}
