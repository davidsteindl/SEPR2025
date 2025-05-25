import {Sector} from "./sector";
import {Location} from "./location";

export interface Room {
  id: number;
  sectors: Sector[];
  name: string;
}
