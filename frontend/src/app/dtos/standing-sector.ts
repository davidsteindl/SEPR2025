import {Sector} from "./sector";
import {SectorType} from "./sector-type";

export class StandingSector extends Sector{

  capacity: number;
  type: SectorType.STANDING;
}
