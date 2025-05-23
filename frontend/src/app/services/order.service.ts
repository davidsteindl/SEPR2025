import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { OrderDto } from '../dtos/order';
import { Page } from '../dtos/page';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private baseUrl = '/api/v1/tickets/orders';

  constructor(private http: HttpClient) {}

  getOrders(type: 'upcoming' | 'reservations' | 'past', page = 0, size = 10) {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<OrderDto>>(`${this.baseUrl}/${type}`, { params });
  }
}
