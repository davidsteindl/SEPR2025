import {Injectable} from '@angular/core';
import {AuthRequest} from '../dtos/auth-request';
import {Observable} from 'rxjs';
import { HttpClient } from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {jwtDecode} from 'jwt-decode';
import {Globals} from '../global/globals';
import {RegisterUser} from "../dtos/register-user";
import {PasswordChange} from "../dtos/password-change";
import { HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authBaseUri: string = this.globals.backendUri + '/authentication';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Login in the user. If it was successful, a valid JWT token will be stored
   *
   * @param authRequest User data
   */
  loginUser(authRequest: AuthRequest): Observable<string> {
    return this.httpClient.post(this.authBaseUri + '/login', authRequest, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => this.setToken(authResponse))
      );
  }

  /**
   * Register a new user. If it was successful, a valid JWT token will be stored
   *
   * @param registerUser User data
   */
  registerUser(registerUser: RegisterUser): Observable<void> {
    console.log("register");
    return this.httpClient.post<void>(this.authBaseUri + '/register', registerUser)

  }


  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn() {
    return !!this.getToken() && (this.getTokenExpirationDate(this.getToken()).valueOf() > new Date().valueOf());
  }

  logoutUser() {
    console.log('Logout');
    localStorage.removeItem('authToken');
  }

  getToken() {
    return localStorage.getItem('authToken');
  }

  /**
   * Returns the user role based on the current token
   */
  getUserRole() {
    if (this.getToken() != null) {
      const decoded: any = jwtDecode(this.getToken());
      const authInfo: string[] = decoded.rol;
      if (authInfo.includes('ROLE_ADMIN')) {
        return 'ADMIN';
      } else if (authInfo.includes('ROLE_USER')) {
        return 'USER';
      }
    }
    return 'UNDEFINED';
  }

  private setToken(authResponse: string) {
    localStorage.setItem('authToken', authResponse);
  }

  private getTokenExpirationDate(token: string): Date {

    const decoded: any = jwtDecode(token);
    if (decoded.exp === undefined) {
      return null;
    }

    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }

  resetPassword(email: string) : Observable<void> {
    console.log("Reset-Password-Request");
    const headers = new HttpHeaders({ 'Skip-Auth': 'true' });
    return this.httpClient.post<void>(this.authBaseUri + '/password-change-requests', { email }, { headers: headers, withCredentials: false })

  }

  changePassword(changePasswordRequest: PasswordChange, token: string): Observable<void> {
    console.log("Change-Password-Request" + token);
    const headers = new HttpHeaders({ 'Skip-Auth': 'true' });
    return this.httpClient.post<void>(`${this.authBaseUri}/password-change-requests/${token}`, changePasswordRequest, { headers })
  }

  /**
   * Returns the user ID from the current JWT token
   */
  getUserId(): number {
    if (this.getToken() != null) {
      const decoded: any = jwtDecode(this.getToken());
      return Number(decoded.sub);
    }
    return null;
  }
}
