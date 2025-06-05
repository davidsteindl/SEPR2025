import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentItem } from '../dtos/payment-item';
import {OrderDto, OrderGroupDetailDto, OrderGroupDto} from '../dtos/order';
import { TicketDto, TicketRequestDto, ReservationDto } from '../dtos/ticket';
import { Globals } from '../global/globals';
import {Page} from "../dtos/page";

@Injectable({ providedIn: 'root' })
export class TicketService {
    private base: string = this.globals.backendUri + '/tickets';


  constructor(private http: HttpClient, private globals: Globals) {}

  buyTickets(
    showId: number,
    items: PaymentItem[],
    paymentForm: {
      cardNumber: string;
      expirationDate: string;
      securityCode: string;
      firstName: string;
      lastName: string;
      street: string;
      housenumber: string;
      postalCode: string;
      city: string;
      country: string;
    }
  ): Observable<OrderDto> {
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

    const payload: TicketRequestDto = {
      showId,
      targets,
      ...paymentForm
    };

    return this.http.post<OrderDto>(`${this.base}/buy`, payload);
  }

  reserveTickets(showId: number, items: PaymentItem[]): Observable<ReservationDto> {
    const targets = items.map(i => {
      if (i.type === 'SEATED') {
        return {
          type: 'seated' as const,
          seatId: i.seatId!,
          sectorId: i.sectorId
        };
      } else {
        return {
          type: 'standing' as const,
          sectorId: i.sectorId,
          quantity: i.quantity!
        };
      }
    });

    const payload: TicketRequestDto = {
      showId,
      targets
    };

    return this.http.post<ReservationDto>(`${this.base}/reserve`, payload);
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

  getOrderGroupsPaged(
    isReservation: boolean,
    past: boolean,
    page: number = 0,
    size: number = 10
  ): Observable<Page<OrderGroupDto>> {
    return this.http.get<Page<OrderGroupDto>>(
      `${this.globals.backendUri}/tickets/order-groups?isReservation=${isReservation}&past=${past}&page=${page}&size=${size}`
    );
  }

  getOrderGroupDetails(groupId: number): Observable<OrderGroupDetailDto> {
    return this.http.get<OrderGroupDetailDto>(`/api/v1/tickets/order-groups/${groupId}`);
  }

}
