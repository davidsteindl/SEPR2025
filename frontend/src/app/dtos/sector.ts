import {SectorType} from "./sector-type";
import {Room} from "./room";

export abstract class Sector {
  id: number;
  type: SectorType;
  price: number;
  room: Room;
}
