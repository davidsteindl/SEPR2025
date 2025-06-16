import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {User, UserEdit} from '../dtos/user';
import {formatIsoDate} from "../utils/date-helper";

const baseUri = environment.backendUrl + '/users';
import {Injectable} from '@angular/core';
import {LockedUser} from '../dtos/locked-user';
import {Globals} from '../global/globals';
import {Page} from "../dtos/page";
import {Message} from "../dtos/message";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private userBaseUri: string = this.globals.backendUri + '/users';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Gets a specific user by their ID.
   *
   * @param id - The ID of the user to fetch.
   * @returns The user object.
   */
  getById(id: string): Observable<User> {
    return this.httpClient.get<User>(`${this.userBaseUri}/${id}`).pipe(map(this.fixUserDate));
  }


  /**
   *  Get current user.
   */
  getCurrentUser(): Observable<User> {
    console.log(' current User ');
    return this.httpClient.get<User>(`${this.userBaseUri}/me`);
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
   * @return Observable, to confirm that deleting was successful.
   */
  deleteUser(): Observable<void> {
    return this.httpClient.delete<void>(
      `${this.userBaseUri}/me`);
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
   * Loads all users from the backend paginated.
   */
  getAllUsersPaginated(page = 0, size = 10): Observable<Page<User>> {
    const params = `?page=${page}&size=${size}`;
    return this.httpClient.get<Page<User>>(`${this.userBaseUri}/paginated${params}`);
  }


  /**
   * Unlocks a user by ID.
   */
  unlockUser(id: number): Observable<void> {
    console.log('Unlock User with id ' + id);
    return this.httpClient.put<void>(`${this.userBaseUri}/${id}/unlock`, null);
  }

  /**
   * Blocks a user by ID.
   */
  blockUser(id: number): Observable<void> {
    console.log('Unlock User with id ' + id);
    return this.httpClient.put<void>(`${this.userBaseUri}/${id}/block`, null);
  }

  /**
   * Sends a password-reset to the user.
   */
  resetPassword(id: number): Observable<void> {
    console.log('Sends a password-reset to user with id resetPassword' + id);
    return this.httpClient.put<void>(`${this.userBaseUri}/${id}/resetPassword`, null);
  }

  /**
   * Loads all UNSEEN messages for the current user
   */
  getUnseenMessages(userId: number): Observable<Message[]> {
    return this.httpClient.get<Message[]>(`${this.userBaseUri}/${userId}/news/unseen`);
  }

  /**
   * Marks a message ID as seen for the current user
   */
  markMessageAsSeen(userId: number, messageId: number): Observable<void> {
    return this.httpClient.post<void>(`${this.userBaseUri}/${userId}/news/${messageId}/markSeen`, null);
  }
}
