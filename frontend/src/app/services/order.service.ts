import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Globals } from '../global/globals';
import { Observable } from 'rxjs';
import { Page } from '../dtos/page';
import { OrderDto } from '../dtos/order';
import { TicketDto } from '../dtos/ticket';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private orderBaseUri: string = this.globals.backendUri + '/tickets/orders';

  constructor(private http: HttpClient, private globals: Globals) {}

  /**
   * Retrieves orders of a specific type for the current user (paginated)
   *
   * @param type one of 'upcoming' | 'reservations' | 'past' | 'refunded'
   * @param page current page index
   * @param size number of elements per page
   */
  getOrders(type: 'upcoming' | 'reservations' | 'past' | 'refunded', page = 0, size = 10): Observable<Page<OrderDto>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<OrderDto>>(`${this.orderBaseUri}/${type}`, { params });
  }

  /**
   * Retrieves the metadata of a single order by its ID (without ticket list).
   */
  getOrderById(orderId: number): Observable<OrderDto> {
    return this.http.get<OrderDto>(`${this.orderBaseUri}/${orderId}`);
  }

  /**
   * Retrieves paginated tickets for a specific order.
   */
  getTicketsForOrder(orderId: number, page = 0, size = 50): Observable<Page<TicketDto>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<TicketDto>>(`${this.orderBaseUri}/${orderId}/tickets`, { params });
  }

}
