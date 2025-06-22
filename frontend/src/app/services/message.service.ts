import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Message, MessageCreate} from '../dtos/message';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {Page} from "../dtos/page";

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  private messageBaseUri: string = this.globals.backendUri + '/news';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Loads all messages from the backend with pagination.
   *
   * @returns Observable of page of messages
   */
  getMessagesPaginated(page: number, size: number): Observable<Page<Message>> {
    return this.httpClient.get<Page<Message>>(`${this.messageBaseUri}/paginated?page=${page}&size=${size}`);
  }

  /**
   * Loads specific message from the backend
   *
   * @param id of message to load
   */
  getMessageById(id: number): Observable<Message> {
    console.log('Load message details for ' + id);
    return this.httpClient.get<Message>(this.messageBaseUri + '/' + id);
  }

  /**
   * Persists message to the backend
   *
   * @param message to persist
   */
  createMessage(message: MessageCreate): Observable<Message> {
    console.log('Create message with title ' + message.title);


    const formData = new FormData();
    formData.append('message', new Blob([JSON.stringify({...message, images: undefined})], {type: 'application/json'}));
    if (message.images) {
      for (const image of message.images) {
        formData.append('images', image, image.name);
      }
    }

    return this.httpClient.post<Message>(this.messageBaseUri, formData);


  }

  /**
   * Gets Image Blob
   *
   * @param messageId of message to load
   * @param imageId of image to load
   */
  getImageBlob(messageId: number, imageId: number): Observable<Blob> {
    return this.httpClient.get(this.messageBaseUri + `/${messageId}/image/${imageId}`, {responseType: 'blob'});
  }
}
