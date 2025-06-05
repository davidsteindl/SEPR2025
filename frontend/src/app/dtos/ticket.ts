export type TicketStatus =
  | 'RESERVED'
  | 'BOUGHT'
  | 'REFUNDED'
  | 'EXPIRED'
  | 'CANCELLED';

export interface TicketDto {
  id: number;
  showName: string;
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
  showId: number;
  targets: (TicketTargetSeatedDto | TicketTargetStandingDto)[];
  cardNumber: string;
  expirationDate: string;
  securityCode: string;
  firstName: string;
  lastName: string;
  housenumber: string;
  country: string;
  city: string;
  street: string;
  postalCode: string;
}
