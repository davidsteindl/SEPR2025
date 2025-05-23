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
