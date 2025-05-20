import {Sector} from "./sector";
import {Seat} from "./seat";
import {SectorType} from "./sector-type";

export class SeatedSector extends Sector  {

  rows: Seat[];
  type = SectorType.SEATED;
}
