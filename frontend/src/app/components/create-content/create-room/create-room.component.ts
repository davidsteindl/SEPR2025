import {Component, OnInit} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {CreateShow} from "../../../dtos/create-show";
import {CreateRoom} from "../../../dtos/create-room";
import {Event} from "../../../dtos/event";
import {Location} from "../../../dtos/location";
import {EventCategory} from "../create-event/create-event.component";
import {EventService} from "../../../services/event.service";
import {LocationService} from "../../../services/location.service";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {Router} from "@angular/router";
import {RoomService} from "../../../services/room.service";

@Component({
  selector: 'app-create-room',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NgIf,
    NgForOf
  ],
  templateUrl: './create-room.component.html',
  styleUrl: './create-room.component.scss'
})
export class CreateRoomComponent implements OnInit {

  room: CreateRoom = {
    name: '',
    numberOfSectors: 3,
    rowsPerSector: 3,
    seatsPerRow: 3,
    eventLocationId: null,
    isHorizontal: true
  };
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
  }


  createRoom(): void {
    this.roomService.create(this.room).subscribe({
      next: () => {
        console.log('Show created:', this.room.name);
        this.nameOfRoom = this.room.name;
        this.room = {
          name: '',
          numberOfSectors: 3,
          rowsPerSector: 3,
          seatsPerRow: 3,
          eventLocationId: null,
          isHorizontal: true
        };
          this.notification.success(`Room ${this.nameOfRoom} created successfully!`, 'Success', {
            enableHtml: true,
            timeOut: 8000,
          });
          this.router.navigate(['/admin']);
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
}
