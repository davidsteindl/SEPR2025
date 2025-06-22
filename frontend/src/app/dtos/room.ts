import {Sector} from "./sector";
import {Location} from "./location";
import { Seat } from "./seat";

export interface Room {
  id: number;
  xSize: number;
  ySize: number;
  seats: Seat[];
  sectors: Sector[];
  name: string;
  eventLocationId: number;
}
export interface RoomPageDto {
  id: number;
  name: string;
  eventLocationName: string;
}
export interface RoomPageDto {
  id: number;
  name: string;
  eventLocationName: string;
}
