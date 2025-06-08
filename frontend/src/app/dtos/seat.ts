export interface Seat {
  id: number | null;  // allow null for new (unsaved) seats
  rowNumber: number;
  columnNumber: number;
  deleted: boolean;
  sectorId?: number;   // optional: backend SeatDto.sectorId
  roomId?: number;     // optional: backend SeatDto.roomId
  available?: boolean;
}
