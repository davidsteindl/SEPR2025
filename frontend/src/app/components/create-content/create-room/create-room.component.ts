import { Component, OnInit, ViewChild } from "@angular/core";
import { FormsModule, NgForm, ReactiveFormsModule } from "@angular/forms";
import { NgForOf, NgIf } from "@angular/common";
import { CreateRoom } from "../../../dtos/create-room";
import { Location } from "../../../dtos/location";
import { LocationService } from "../../../services/location.service";
import { ToastrService } from "ngx-toastr";
import { ErrorFormatterService } from "../../../services/error-formatter.service";
import { Router } from "@angular/router";
import { RoomService } from "../../../services/room.service";
import { Room } from "../../../dtos/room";

@Component({
  selector: "app-create-room",
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, NgIf, NgForOf],
  templateUrl: "./create-room.component.html",
  styleUrls: ["./create-room.component.scss"],
})
export class CreateRoomComponent implements OnInit {
  @ViewChild("roomForm") form!: NgForm;

  // now using rows & columns only
  room: CreateRoom = {
    name: "",
    rows: 3,
    columns: 3,
    eventLocationId: null,
  };
  private initialRoom!: CreateRoom;
  locations: Location[] = [];
  showConfirm = false;

  constructor(
    private locationService: LocationService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private roomService: RoomService,
    private router: Router
  ) {}

  ngOnInit() {
    this.locationService.getAll().subscribe({
      next: (locs) => (this.locations = locs),
      error: (err) => {
        console.error(err);
        this.notification.error(
          this.errorFormatter.format(err),
          "Error while fetching locations",
          { enableHtml: true, timeOut: 8000 }
        );
      },
    });
    // keep a pristine copy for “dirty” check
    this.initialRoom = { ...this.room };
  }

  createRoom(): void {
    this.roomService.create(this.room).subscribe({
      next: (room: Room) => {
        const createdName = this.room.name;
        this.notification.success(
          `Room ${createdName} created successfully!`,
          "Success",
          { enableHtml: true, timeOut: 8000 }
        );
        // reset form
        this.room = { name: "", rows: 3, columns: 3, eventLocationId: null };
        this.form.resetForm();
        this.initialRoom = { ...this.room };
        // navigate into new room overview
        this.router.navigate(["/rooms", room.id, "overview"]);
      },
      error: (err) => {
        console.error(err);
        this.notification.error(
          this.errorFormatter.format(err),
          "Error while creating room",
          { enableHtml: true, timeOut: 8000 }
        );
      },
    });
  }

  onBackClick(): void {
    if (JSON.stringify(this.initialRoom) === JSON.stringify(this.room)) {
      this.router.navigate(["/admin"]);
    } else {
      this.showConfirm = true;
    }
  }

  validateRows(): void {
    if (this.room.rows == null) {
      return;
    }
    if (this.room.rows < 1) {
      this.room.rows = 1;
    } else if (this.room.rows > 100) {
      this.room.rows = 100;
    }
  }

  validateColumns(): void {
    if (this.room.columns == null) {
      return;
    }
    if (this.room.columns < 1) {
      this.room.columns = 1;
    } else if (this.room.columns > 100) {
      this.room.columns = 100;
    }
  }

  preventNonNumericInput(event: KeyboardEvent): void {
    const invalidChars = ['e', 'E', '+', '-', '.'];
    if (invalidChars.includes(event.key)) {
      event.preventDefault();
    }
  }

  stay(): void {
    this.showConfirm = false;
  }
  exit(): void {
    this.showConfirm = false;
    this.router.navigate(["/admin"]);
  }
}
