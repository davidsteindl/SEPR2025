import {Component, OnInit, ViewChild} from '@angular/core';
import {FormsModule, NgForm} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {ShowService} from '../../../services/show.service';
import {EventService} from '../../../services/event.service';
import {ArtistService} from '../../../services/artist.service';
import {CreateShow} from '../../../dtos/create-show';
import {Event} from '../../../dtos/event';
import {Artist} from '../../../dtos/artist';
import {ToastrService} from 'ngx-toastr';
import {ErrorFormatterService} from '../../../services/error-formatter.service';
import {Router, RouterLink} from '@angular/router';
import {Room} from "../../../dtos/room";
import {RoomService} from "../../../services/room.service";

@Component({
  selector: 'app-create-show',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
  ],
  templateUrl: './create-show.component.html',
  styleUrl: './create-show.component.scss'
})
export class CreateShowComponent implements OnInit {
  @ViewChild('showForm') form!: NgForm;

  show: CreateShow = {
    name: '',
    duration: 60,
    date: '',
    eventId: null,
    artistIds: [],
    roomId: null
  };

  private initialShow!: CreateShow;

  events: Event[] = [];
  artists: Artist[] = [];
  rooms: Room[] = [];

  constructor(
    private showService: ShowService,
    private eventService: EventService,
    private artistService: ArtistService,
    private roomService: RoomService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.eventService.getAll().subscribe({
      next: (result) => {
        this.events = result;
      },
      error: (err) => {
        console.error('Error fetching events:', err);
        this.notification.error(this.errorFormatter.format(err), 'Error while fetching events', {
          enableHtml: true,
          timeOut: 8000,
        });

      }
    });

    this.artistService.getAll().subscribe({
      next: (result) => {
        this.artists = result;
      },
      error: (err) => {
        console.error('Error fetching artists:', err);
        this.notification.error(this.errorFormatter.format(err), 'Error while fetching artists', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });

    this.roomService.getAll().subscribe({
      next: (result) => this.rooms = result,
      error: (err) => {
        console.error('Error fetching rooms:', err);
        this.notification.error(this.errorFormatter.format(err), 'Error while fetching rooms', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
    this.initialShow = JSON.parse(JSON.stringify(this.show));
  }

  isArtistSelected(artistId: number): boolean {
    return this.show.artistIds.includes(artistId);
  }

  toggleArtistSelection(artistId: number): void {
    const index = this.show.artistIds.indexOf(artistId);
    if (index > -1) {
      this.show.artistIds.splice(index, 1);
    } else {
      this.show.artistIds.push(artistId);
    }
  }


  createShow(): void {
    this.showService.create(this.show).subscribe({
      next: (createdShow) => {
        console.log('Show created:', createdShow);
        this.show = {
          name: '',
          duration: 60,
          date: '',
          eventId: null,
          artistIds: [],
          roomId: null
        };
        this.form.resetForm();
        this.initialShow = JSON.parse(JSON.stringify(this.show));
        if (createdShow) {
          this.notification.success(`Show ${createdShow.name} created successfully!`, 'Success', {
            enableHtml: true,
            timeOut: 8000,
          });
          this.router.navigate(['/admin']);
        }
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

  preventNonNumericInput(event: KeyboardEvent): void {
    const invalidChars = ['e', 'E', '+', '-', '.'];
    if (invalidChars.includes(event.key)) {
      event.preventDefault();
    }
  }

  validateDuration(): void {
    if (this.show.duration == null) {
      return;
    }

    if (this.show.duration < 10) {
      this.show.duration = 10;
    } else if (this.show.duration > 600) {
      this.show.duration = 600;
    }
  }

  showConfirm: boolean = false;

  private isUnchanged(): boolean {
    return (
      JSON.stringify(this.initialShow) === JSON.stringify(this.show)
    );
  }

  onBackClick(): void {
    if (this.isUnchanged()) {
      this.router.navigate(['/admin']);
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

  preventNonNumericInput(event: KeyboardEvent): void {
    const invalidChars = ['e', 'E', '+', '-', '.'];
    if (invalidChars.includes(event.key)) {
      event.preventDefault();
    }
  }
}
