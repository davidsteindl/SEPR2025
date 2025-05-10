import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {User, UserEdit } from '../dtos/user';
import {formatIsoDate} from "../utils/date-helper";
const baseUri = environment.backendUrl + '/users';
import { Injectable } from '@angular/core';
import { LockedUser } from '../dtos/locked-user';
import { Globals } from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private userBaseUri: string = this.globals.backendUri + '/users';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Get a specific user stored in the system
   *
   * @return user found by id.
   */
  getById(id: string): Observable<User> {
    return this.httpClient.get<User>(`${baseUri}/${id}`)
      .pipe(
        map(this.fixUserDate)
      );
  }


  /**
   * Edit an existing user in the system.
   *
   * @param user the data for the user that should be updated
   * @return an Observable for the updated user
   */
  edit(user: UserEdit): Observable<void> {
    console.log(user);

    (user as any).dateOfBirth = formatIsoDate(user.dateOfBirth);

      return this.httpClient.put<void>(`${this.userBaseUri}/me`, user);
  }


  /**
   * Deleting an existing user.
   *
   * @param id the ID of the user to delete.
   * @return Observable, to confirm that deleting was successful.
   */
  deleteUser(id: number): Observable<void> {
    return this.httpClient.delete<void>(
      `${baseUri}/${id}`);
  }


  /**
   * Converts the dateOfBirth property of a horse to a JavaScript Date object.
   *
   * This method ensures that the dateOfBirth is parsed correctly into a Date object
   * for consistent handling within the application.
   *
   * @param user the horse object whose dateOfBirth should be fixed
   * @return the user object with a corrected dateOfBirth as a Date
   */
  private fixUserDate(user: User): User {
    // Parse the string to a Date
    user.dateOfBirth = new Date(user.dateOfBirth as unknown as string);
    return user;
  }


  /**
   * Loads all locked users from the backend.
   */
  getLockedUsers(): Observable<LockedUser[]> {
    console.log("getting locked users")
    return this.httpClient.get<LockedUser[]>(`${this.userBaseUri}/locked`);
  }


  /**
   * Unlocks a user by ID.
   */
  unlockUser(id: number): Observable<void> {
    console.log('Unlock User with id ' + id);
    return this.httpClient.put<void>(`${this.userBaseUri}/${id}/unlock`, null);
  }


  /**
   *  Get current user.
   */
  getCurrentUser(): Observable<User> {
    console.log(' current User ' );
    return this.httpClient.get<User>(`${this.userBaseUri}/me`);
  }

}
