import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LockedUser } from '../dtos/locked-user';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private userBaseUri: string = this.globals.backendUri + '/users';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }


  /**
    * Loads all locked users from the backend
    */
  getLockedUsers(): Observable<LockedUser[]> {
    console.log("getting locked users")
    return this.httpClient.get<LockedUser[]>(`${this.userBaseUri}/locked`);
  }


  /**
   * Unlocks a user by ID
   */
  unlockUser(id: number): Observable<void> {
    console.log('Unlock User with id ' + id);
    return this.httpClient.put<void>(`${this.userBaseUri}/${id}/unlock`, null);
  }
}
