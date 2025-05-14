export interface Artist {
  id: number;
  firstname: string;
  lastname: string;
  stagename: string;
  showIds: number[];
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
