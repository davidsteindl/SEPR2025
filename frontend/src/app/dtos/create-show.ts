export interface CreateShow {
  name: string;
  duration: number; // in minutes
  date: string; // Format: "YYYY-MM-DDThh:mm:ss", e.g. "2023-10-01T20:00:00"
  eventId: number;
  artistIds: number[];
  roomId: number;
}
