import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NgForm, FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {EventService} from '../../services/event.service';
import {CreateEvent} from '../../dtos/create-event';
import {LocationService} from '../../services/location.service';
import {Location} from '../../dtos/location';
import {eventCategoryOptions} from '../create-content/create-event/create-event.component';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-edit-event',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-event.component.html',
  styleUrls: ['./edit-event.component.scss']
})
export class EditEventComponent implements OnInit {
  eventId!: number;
  event: CreateEvent = {
    name: '',
    description: '',
    dateTime: '',
    duration: 60,
    category: null,
    locationId: null
  };

  locations: Location[] = [];
  eventCategoryOptions = eventCategoryOptions;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private locationService: LocationService,
    private toastr: ToastrService
  ) {
  }

  ngOnInit(): void {
    this.locationService.getAll().subscribe(loc => this.locations = loc);

    this.eventId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.eventId) {
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
          this.loading = false;
        },
        error: err => {
          this.toastr.error('Could not load event details');
          this.router.navigate(['/admin/events']);
        }
      });
    }
  }

  save(form: NgForm): void {
    if (form.invalid) return;

    this.eventService.update(this.eventId, this.event).subscribe({
      next: () => {
        this.toastr.success('Event updated successfully');
        this.router.navigate(['/admin/events']);
      },
      error: () => {
        this.toastr.error('Error saving event');
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/events']);
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
    const dt = new Date(iso);
    const tzOffsetMs = dt.getTimezoneOffset() * 60000;
    const localISO = new Date(dt.getTime() - tzOffsetMs)
      .toISOString()
      .slice(0, 16);
    return localISO;
  }
}
