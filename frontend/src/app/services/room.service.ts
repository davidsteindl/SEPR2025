import { Injectable } from '@angular/core';
import {Globals} from '../global/globals';
import {HttpClient} from "@angular/common/http";
import {CreateRoom} from "../dtos/create-room";
import { Observable } from 'rxjs';
import {Room, RoomPageDto} from "../dtos/room";
import { Page } from '../dtos/page';


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
  create(room: CreateRoom): Observable<Room> {
    console.log('Create room with name: ' + room.name);
    return this.httpClient.post<Room>(this.roomBaseUri, room);
  }

  /**
   * Creates a new room
   *
   * @param room to create
   */
  edit(room: Room): Observable<Room> {
    console.log('Edit room with name: ' + room.name);
    return this.httpClient.put<Room>(`${this.roomBaseUri}/${room.id}`, room);
  }

  /**
   * Retrieves the room with the given ID
   *
   * @param id ID of the room to retrieve
   */
  getRoomById(id: number): Observable<Room> {
    return this.httpClient.get<Room>(`${this.roomBaseUri}/${id}`);
  }

  /**
   * Retrieves all rooms from the backend.
   *
   * @returns An {@link Observable} emitting an array of {@link Room}
   */
  getAll(): Observable<Room[]> {
    return this.httpClient.get<Room[]>(this.roomBaseUri);
  }

  /**
   * Retrieves all rooms paginated.
   */
  getPaginatedRooms(page: number, size: number): Observable<Page<RoomPageDto>> {
    return this.httpClient.get<Page<RoomPageDto>>(
      `${this.roomBaseUri}/paginated?page=${page}&size=${size}`
    );
  }
}
