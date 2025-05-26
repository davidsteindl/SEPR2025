export interface Show {
  id: number;
  name: string;
  duration: number; // in minutes
  date: string; // Format: "YYYY-MM-DDThh:mm:ss", e.g. "2023-10-01T20:00:00"
  eventId: number;
  artistIds: number[];
  roomId: number;
}

export interface ShowSearch {
  page: number;
  size: number;
  name?: string;
  eventName?: string;
  roomName?: string;
  startDate?: string;
  endDate?: string;
  minPrice?: number;
  maxPrice?: number;
}

export interface ShowSearchResult {
  id: number;
  name: string;
  duration: number;
  date: string;
  eventId: number;
  eventName: string;
  roomId: number;
  roomName: string;
  minPrice: number | null;
  maxPrice: number | null;
}
