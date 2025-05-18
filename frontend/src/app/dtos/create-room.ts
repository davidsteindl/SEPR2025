export interface CreateRoom {
  name: string,
  amountSectors: number,
  amountRows: number,
  seatPerRows: number,
  locationId: number,
  isHorizontal: boolean
}
