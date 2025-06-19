export interface CreateRoom {
  name: string;
  rows: number;
  columns: number;
  eventLocationId: number | null;
}
