import {Sector} from "./sector";
import {Seat} from "./seat";
import {SectorType} from "./sector-type";

export class SeatedSector extends Sector {
  seats: Seat[];
  type = SectorType.NORMAL;
}
