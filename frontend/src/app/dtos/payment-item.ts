export interface PaymentItem {
  eventName: string;
  type: 'SEATED' | 'STANDING';
  price: number;
  sectorId: number;
  seatId?: number;
  rowNumber?: number;
  columnNumber?: number;
  quantity?: number;
  showId?: number;
}
