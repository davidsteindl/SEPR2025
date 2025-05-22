import { TicketDto, TicketStatus } from './ticket';

export type PaymentType    = 'CREDIT_CARD';
export type OrderType      = 'ORDER' | 'RESERVATION' | 'REFUND';

export interface OrderDto {
  id: number;
  createdAt: string;      // ISO timestamp
  tickets: TicketDto[];
  paymentType: PaymentType;
  userId: number;
  orderType: OrderType;
}