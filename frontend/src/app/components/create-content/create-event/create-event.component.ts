import {Component, OnInit} from '@angular/core';
import {CreateEvent} from '../../../dtos/create-event';
import {Event} from '../../../dtos/event';
import {Location} from '../../../dtos/location';
import {FormsModule} from "@angular/forms";
import {CommonModule, NgForOf} from "@angular/common";
import {EventService} from '../../../services/event.service';
import {LocationService} from '../../../services/location.service';

@Component({
  selector: 'app-create-event',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgForOf
  ],
  templateUrl: './create-event.component.html',
  styleUrl: './create-event.component.scss'
})

export class CreateEventComponent implements OnInit {
  event: CreateEvent = {
    name: '',
    description: '',
    duration: 60,
    category: null,
    locationId: null
  }

  createdEvent: Event = null;
  locationNameOfCreatedEvent: String = null;

  eventCategories: string[] = [
    'Classical', 'Jazz', 'Rock', 'Pop', 'Electronic', 'Hip-Hop', 'Country', 'Reggae', 'Folk', 'Opera',
    'Musical', 'Alternative', 'Latin', 'R&B', 'Metal', 'Indie', 'Theatre', 'Comedy', 'Ballet', 'Other'
  ];

  locations: Location[] = [];

  constructor(
    private eventService: EventService,
    private locationService: LocationService
  ) {
  }

  ngOnInit() {
    this.locationService.getAll().subscribe({
      next: (locations) => {
        this.locations = locations;
      },
      error: (err) => {
        console.error('Error while fetching locations:', err);
      }
    });
  }

  createEvent() {
    this.eventService.create(this.event).subscribe({
      next: (response) => {
        this.createdEvent = response;
        this.locationService.getLocationById(this.createdEvent.locationId).subscribe({
          next: (location) => {
            this.locationNameOfCreatedEvent = location.name;
          },
          error: (err) => {
            console.error('Error while fetching location name:', err);
          }
        });
        this.event = {name: '', description: '', duration: 60, category: null, locationId: null};
      },
      error: (err) => {
        console.error('Error while creating event:', err);
      }
    });
  }
}
