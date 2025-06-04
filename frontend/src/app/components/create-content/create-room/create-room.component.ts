import {Component, OnInit, ViewChild} from '@angular/core';
import {FormsModule, NgForm, ReactiveFormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {CreateRoom} from "../../../dtos/create-room";
import {Location} from "../../../dtos/location";
import {LocationService} from "../../../services/location.service";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {Router} from "@angular/router";
import {RoomService} from "../../../services/room.service";
import {Room} from "../../../dtos/room";

@Component({
  selector: 'app-create-room',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NgIf,
    NgForOf,
  ],
  templateUrl: './create-room.component.html',
  styleUrl: './create-room.component.scss'
})
export class CreateRoomComponent implements OnInit {
  @ViewChild('roomForm') form!: NgForm;

  room: CreateRoom = {
    name: '',
    numberOfSectors: 3,
    rowsPerSector: 3,
    seatsPerRow: 3,
    eventLocationId: null,
  };
  private initialRoom!: CreateRoom;

  nameOfRoom: string;
  locations: Location[] = [];

  constructor(
    private locationService: LocationService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private roomService: RoomService,
    private router: Router
  ) {
  }

  ngOnInit() {
    this.locationService.getAll().subscribe({
      next: (locations) => {
        this.locations = locations;
      },
      error: (err) => {
        console.error('Error while fetching locations:', err);
        this.notification.error(this.errorFormatter.format(err), 'Error while fetching locations', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
    this.initialRoom = JSON.parse(JSON.stringify(this.room));
  }


  createRoom(): void {
    this.roomService.create(this.room).subscribe({
      next: (room: Room) => {
        console.log('Show created:', this.room.name);
        this.nameOfRoom = this.room.name;
        this.room = {
          name: '',
          numberOfSectors: 3,
          rowsPerSector: 3,
          seatsPerRow: 3,
          eventLocationId: null,
        };
        this.form.resetForm();
        this.initialRoom = JSON.parse(JSON.stringify(this.room));
        this.notification.success(`Room ${this.nameOfRoom} created successfully!`, 'Success', {
          enableHtml: true,
          timeOut: 8000,
        });
        this.router.navigate(['/rooms', room.id, 'overview']);
      },
      error: (err) => {
        console.error('Error creating room:', err);
        this.notification.error(this.errorFormatter.format(err), 'Error while creating room', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }

  showConfirm: boolean = false;

  private isUnchanged(): boolean {
    return JSON.stringify(this.initialRoom) === JSON.stringify(this.room);
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
}
