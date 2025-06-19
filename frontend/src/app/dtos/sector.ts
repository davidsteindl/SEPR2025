import {SectorType} from "./sector-type";
import {Room} from "./room";
import { Seat } from "./seat";

export abstract class Sector {
  id: number;
  type: SectorType;
  price: number;
  room: Room;
  seats: Seat[];
  capacity?: number;
  availableCapacity?: number;
}

export { SectorType };
