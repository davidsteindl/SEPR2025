import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Globals} from '../global/globals';
import {Observable} from 'rxjs';
import {EventLocationSearchDto, Location} from '../dtos/location';
import {CreateLocation} from "../dtos/create-location";
import {Page} from "../dtos/page";
import {ShowSearchResult} from "../dtos/show";

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

  /**
   * Retrieves all eventlocations specified by the search criteria, paginated.
   *
   * @param criteria Search criteria for filtering eventlocations
   * @returns An Observable of a paginated list of location objects
   */
  searchEventLocations(criteria: EventLocationSearchDto): Observable<Page<Location>> {
    return this.httpClient.post<Page<Location>>(
      this.locationBaseUri + '/search',
      criteria
    );
  }

  /**
   * Retrieves all events for the given location ID
   *
   * @param locationId ID of the location to retrieve events for
   */
  getShowsForEventLocation(locationId: number, page: number, size: number): Observable<Page<ShowSearchResult>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.httpClient
      .get<Page<ShowSearchResult>>(
        `${this.locationBaseUri}/${locationId}/shows/paginated`,
        { params }
      );
  }


}
