import {SectorType} from "./sector-type";
import {Room} from "./room";
import { Seat } from "./seat";

export interface Sector {
  id: number;
  name?: string; // Optional sector name
  type: SectorType;
  price: number;
  room: Room;
  seats: Seat[];
  capacity?: number;
  availableCapacity?: number;
}

export { SectorType };
