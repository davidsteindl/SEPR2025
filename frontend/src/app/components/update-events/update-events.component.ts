import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NgForOf } from '@angular/common';
import { EventService } from '../../services/event.service';
import { Event } from '../../dtos/event';
import { LocationService } from '../../services/location.service';
import { Location } from '../../dtos/location';
import { ToastrService } from 'ngx-toastr';
import { ErrorFormatterService } from '../../services/error-formatter.service';

@Component({
  selector: 'app-update-events',
  standalone: true,
  imports: [
    CommonModule,
    NgForOf,
    RouterLink
  ],
  templateUrl: './update-events.component.html',
  styleUrls: ['./update-events.component.scss']
})
export class UpdateEventsComponent implements OnInit {
  events: Event[] = [];
  loading = false;
  // Map von locationId -> locationName
  locationMap: Record<number, string> = {};

  constructor(
    private eventService: EventService,
    private locationService: LocationService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) { }

  ngOnInit(): void {
    this.loadLocationsAndEvents();
  }

  private loadLocationsAndEvents(): void {
    this.loading = true;

    this.locationService.getAll().subscribe({
      next: (locs: Location[]) => {
        locs.forEach(loc => {
          this.locationMap[loc.id] = loc.name;
        });

        this.eventService.getAll().subscribe({
          next: evs => {
            const now = new Date();

            this.events = evs.filter(ev => {
              const eventStart = new Date(ev.dateTime);
              return eventStart >= now;
            });

            this.loading = false;
          },
          error: err => {
            console.error('Error loading events:', err);
            this.notification.error(
              this.errorFormatter.format(err),
              'Error while fetching events',
              { enableHtml: true, timeOut: 8000 }
            );
            this.loading = false;
          }
        });
      },
      error: err => {
        console.error('Error loading locations:', err);
        this.notification.error(
          this.errorFormatter.format(err),
          'Error while fetching locations',
          { enableHtml: true, timeOut: 8000 }
        );
        this.loading = false;
      }
    });
  }
}
