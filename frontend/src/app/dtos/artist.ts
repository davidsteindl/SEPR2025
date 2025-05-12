import {Show} from "./show";

export interface Artist {
  id: number;
  firstname: string;
  lastname: string;
  stagename: string;
  shows?: Show[]
}

export interface ArtistDto {
  id: number;
  firstname: string;
  lastname: string;
  stagename: string;
}

export interface ArtistSearchDto {
  firstname?: string;
  lastname?: string;
  stagename?: string;
}
