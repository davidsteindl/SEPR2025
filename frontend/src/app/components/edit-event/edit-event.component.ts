import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NgForm, FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {EventService} from '../../services/event.service';
import {CreateEvent} from '../../dtos/create-event';
import {LocationService} from '../../services/location.service';
import {Location} from '../../dtos/location';
import {eventCategoryOptions} from '../create-content/create-event/create-event.component';
import {ToastrService} from 'ngx-toastr';
import {ErrorFormatterService} from '../../services/error-formatter.service';

@Component({
  selector: 'app-edit-event',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-event.component.html',
  styleUrls: ['./edit-event.component.scss']
})

export class EditEventComponent implements OnInit {
  @ViewChild('editEventForm') form!: NgForm;

  eventId!: number;
  event: CreateEvent = {
    name: '',
    description: '',
    dateTime: '',
    duration: 60,
    category: null,
    locationId: null
  };

  private initialEvent!: CreateEvent;

  locations: Location[] = [];
  eventCategoryOptions = eventCategoryOptions;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private locationService: LocationService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

  ngOnInit(): void {
    this.locationService.getAll().subscribe({
      next: locs => this.locations = locs,
      error: err => {
        console.error('Error fetching locations:', err);
        this.notification.error(
          this.errorFormatter.format(err),
          'Error while fetching locations',
          { enableHtml: true, timeOut: 8000 }
        );
      }
    });

    this.eventId = Number(this.route.snapshot.paramMap.get('id'));
    if (isNaN(this.eventId)) {
      this.router.navigate(['/admin']);
      return;
    }
      this.loading = true;
      this.eventService.getEventById(this.eventId).subscribe({
        next: ev => {
          this.event = {
            name: ev.name,
            description: ev.description,
            dateTime: this.toDateTimeLocal(ev.dateTime),
            duration: ev.duration,
            category: ev.category,
            locationId: ev.locationId
          };
          this.initialEvent = JSON.parse(JSON.stringify(this.event));
          this.loading = false;
        },
        error: (err) => {
          console.error('Error creating show:', err);
          this.notification.error(this.errorFormatter.format(err), 'Error while creating show', {
            enableHtml: true,
            timeOut: 8000,
          });
        }
      });
  }

  save(form: NgForm): void {
    if (form.invalid) return;

    this.eventService.update(this.eventId, this.event).subscribe({
      next: () => {
        this.notification.success('Event updated successfully');
        this.initialEvent = JSON.parse(JSON.stringify(this.event));
        this.router.navigate([`/update-events`]);
      },
      error: (err) => {
        this.notification.error(
          this.errorFormatter.format(err),
          'Saving event changes failed',
          {
            enableHtml: true,
            timeOut: 8000,
          }
        );
      }
    });
  }

  validateDuration(): void {
    if (this.event.duration == null) {
      return;
    }

    if (this.event.duration < 10) {
      this.event.duration = 10;
    } else if (this.event.duration > 10000) {
      this.event.duration = 10000;
    }
  }

  private toDateTimeLocal(iso: string): string {
    return iso.slice(0, 16);
  }
  preventNonNumericInput(event: KeyboardEvent): void {
    const invalidChars = ['e', 'E', '+', '-', '.'];
    if (invalidChars.includes(event.key)) {
      event.preventDefault();
    }
  }

  showConfirm: boolean = false;

  private isUnchanged(): boolean {
    return JSON.stringify(this.initialEvent) === JSON.stringify(this.event);
  }

  onBackClick(): void {
    if (this.isUnchanged()) {
      this.router.navigate(['/update-events']);
    } else {
      this.showConfirm = true;
    }
  }

  stay(): void {
    this.showConfirm = false;
  }

  exit(): void {
    this.showConfirm = false;
    this.router.navigate(['/admin']);
  }
}
