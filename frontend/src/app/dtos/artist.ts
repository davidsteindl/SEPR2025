import {Show} from "./show";

export interface Artist {
  id: number;
  firstname: string;
  lastname: string;
  stagename: string;
  shows?: Show[]
}
