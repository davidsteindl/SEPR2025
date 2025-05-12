import { Location } from './location';

export interface CreateEvent {
  name: string;
  category: string;
  location: Location;
}
