import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentItem } from '../dtos/payment-item';
import { OrderDto }  from '../dtos/order';
import { TicketDto } from '../dtos/ticket';
import { Globals } from '../global/globals';

// Backend DTOs
interface TicketTargetSeatedDto {
  type: 'seated';
  sectorId: number;
  seatId: number;
}
interface TicketTargetStandingDto {
  type: 'standing';
  sectorId: number;
  quantity: number;
}

interface TicketRequestDto {
  showId: number;
  targets: (TicketTargetSeatedDto | TicketTargetStandingDto)[];
}



@Injectable({ providedIn: 'root' })
export class TicketService {
    private base: string = this.globals.backendUri + '/tickets';


  constructor(private http: HttpClient, private globals: Globals) {}

  buyTickets(showId: number, items: PaymentItem[]): Observable<OrderDto> {
    const targets = items.map(i => {
      if (i.type === 'SEATED') {
        return {
          type: 'seated' as const,
          sectorId: i.sectorId,
          seatId: i.seatId!
        };
      } else {
        return {
          type: 'standing' as const,
          sectorId: i.sectorId,
          quantity: i.quantity!
        };
      }
    });

    const payload: TicketRequestDto = { showId, targets };
    return this.http.post<OrderDto>(`${this.base}/buy`, payload);
  }

  refundTickets(ticketIds: number[]): Observable<TicketDto[]> {
    return this.http.post<TicketDto[]>(
      `${this.base}/refund`,
      ticketIds
    );
  }

  buyReservedTickets(orderId: number, ticketIds: number[]): Observable<OrderDto> {
    return this.http.post<OrderDto>(
      `${this.globals.backendUri}/tickets/reservations/${orderId}/buy`,
      ticketIds
    );
  }

  cancelReservations(ticketIds: number[]): Observable<TicketDto[]> {
    return this.http.post<TicketDto[]>(
      `${this.globals.backendUri}/tickets/cancel-reservations`,
      ticketIds
    );
  }

}
