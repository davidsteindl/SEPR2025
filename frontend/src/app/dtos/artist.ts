import {Show} from "./show";

export interface Artist {
  id: number;
  firstname: string;
  lastname: string;
  stagename: string;
  shows?: Show[]
}

export interface ArtistSearchDto {
  firstname?: string;
  lastname?: string;
  stagename?: string;
  page?: number;
  size?: number;
}

export interface ArtistSearchResultDto {
  id: number;
  firstname: string;
  lastname: string;
  stagename: string;
}
