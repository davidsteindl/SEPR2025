export type TicketStatus =
  | 'RESERVED'
  | 'BOUGHT'
  | 'REFUNDED'
  | 'EXPIRED'
  | 'CANCELLED';

export type OrderType = 'ORDER' | 'RESERVATION' | 'REFUND' | 'CANCELLATION';

export interface TicketDto {
  id: number;
  showName: string;
  showId?: number;
  price: number;
  seatId: number | null;
  sectorId: number;
  status: TicketStatus;
  rowNumber: number;
  seatLabel: string;
}

export interface TicketTargetSeatedDto {
  type: 'seated';
  seatId: number;
  sectorId: number;
}

export interface TicketTargetStandingDto {
  type: 'standing';
  sectorId: number;
  quantity: number;
}

export interface TicketRequestDto {
  showId?: number;
  targets?: (TicketTargetSeatedDto | TicketTargetStandingDto)[];
  reservedTicketIds?: number[];

  cardNumber?: string;
  expirationDate?: string;
  securityCode?: string;

  firstName?: string;
  lastName?: string;
  street?: string;
  housenumber?: string;
  postalCode?: string;
  city?: string;
  country?: string;
}

export interface ReservationDto {
  id: number;
  createdAt: string;
  tickets: TicketDto[];
  userId: number;
  orderType: 'RESERVATION';
  expiresAt: string;
}

