export interface Event {
  id: number;
  name: string;
  description: string;
  duration: number;
  date: string;
  soldTickets: number;
  locationId: number;
  category: string;
}

export interface EventSearchDto {
  name?: string;
  category?: string;
  duration?: number;
  description?: string;
  page?: number;
  size?: number;
}

export interface EventSearchResultDto {
  id: number;
  name: string;
  category: string;
  locationId: number;
  duration: number;
  description: string;
}
