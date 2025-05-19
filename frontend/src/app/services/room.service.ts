import { Injectable } from '@angular/core';
import {Globals} from '../global/globals';
import {HttpClient} from "@angular/common/http";
import {CreateRoom} from "../dtos/create-room";
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class RoomService {

  private roomBaseUri: string = this.globals.backendUri + '/rooms';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Creates a new room
   *
   * @param room to create
   */
  create(room: CreateRoom): Observable<void> {
    console.log('Create show with name: ' + room.name);
    return this.httpClient.post<void>(this.roomBaseUri, room);
  }
}
