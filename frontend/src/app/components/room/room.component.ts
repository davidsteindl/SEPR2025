import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {RoomService} from "../../services/room.service";
import {Location} from "../../dtos/location";
import {Room} from "../../dtos/room";
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {LocationType} from "../create-content/create-location/create-location.component";
import {StandingSector} from "../../dtos/standing-sector";
import {SectorType} from "../../dtos/sector-type";
import {SeatedSector} from "../../dtos/seated-sector";
import {Seat} from "../../dtos/seat";
import {Sector} from "../../dtos/sector";

@Component({
  selector: 'app-room',
  imports: [
    NgIf,
    NgClass,
    RouterLink,
    NgForOf
  ],
  templateUrl: './room.component.html',
  styleUrl: './room.component.scss'
})
export class RoomComponent implements OnInit{

  room: Room | null = null;

 // Testwerte zum testen bis GetById im Backend fertig ist
  testLocation: Location = {
    id: 1,
    name: "TestLocation",
    type: LocationType.STADIUM,
    country: "Austria",
    city: "Vienna",
    street: "Stadiongasse",
    postalCode: "1200"
  }

  testSeat1: Seat = {
    id: 1,
    rowNumber: 1,
    columnNumber: 1,
    deleted: false
  }

  testSeat2: Seat = {
    id: 2,
    rowNumber: 1,
    columnNumber: 2,
    deleted: false
  }

  testSeat3: Seat = {
    id: 3,
    rowNumber: 2,
    columnNumber: 1,
    deleted: false
  }

  testSeat4: Seat = {
    id: 4,
    rowNumber: 2,
    columnNumber: 2,
    deleted: false
  }

  testSector1: StandingSector = {
    id: 1,
    capacity: 100,
    price: 10,
    room: null,
    type: SectorType.STANDING
  }
  testSector2: SeatedSector = {
    id: 2,
    rows: [this.testSeat1,this.testSeat2,this.testSeat3,this.testSeat4],
    price: 10,
    room: null,
    type: SectorType.SEATED
  }


  testRoom: Room = {
    id: 2,
    sectors: [this.testSector1, this.testSector2],
    name: "Testroom",
    eventLocation: this.testLocation
  }

  selectedSeat: Seat;


  constructor(private route: ActivatedRoute,
              private roomService: RoomService,
              private notification: ToastrService,
              private errorFormatter: ErrorFormatterService
  ) {
  }

  ngOnInit(): void {
    //this.getRoomById();
    this.room = this.testRoom;
    console.log(this.room);
  }

  getRoomById(): void {
    const eventId = Number(this.route.snapshot.paramMap.get('id'));

    this.roomService.getEventById(eventId).subscribe({
      next: room => {
        this.room = room;

      },
      error: err =>  {
        this.notification.error(this.errorFormatter.format(err), 'Loading events failed', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }

  asSeatedSector(sector: Sector): SeatedSector | undefined {
    if ((sector as SeatedSector).rows !== undefined) {
      return sector as SeatedSector;
    }
    return undefined;
  }

  getMaxRows(sector: SeatedSector): number[] {
    const max = Math.max(...sector.rows.map(seat => seat.rowNumber));
    return Array.from({ length: max }, (_, i) => i + 1);
  }

  getMaxColumns(sector: SeatedSector): number[] {
    const max = Math.max(...sector.rows.map(seat => seat.columnNumber));
    return Array.from({ length: max }, (_, i) => i + 1);
  }

  toColumnLetter(col: number): string {
    return String.fromCharCode(96 + col); // 1 -> 'a', 2 -> 'b', ...
  }


  onSeatClick(sector: SeatedSector, row: number, col: number) {
    const seat = sector.rows.find(s => s.rowNumber === row && s.columnNumber === col && !s.deleted);
    if (seat) {
      this.selectedSeat = seat;
      console.log(`Clicked seat ${seat.rowNumber}${this.toColumnLetter(seat.columnNumber)}`);
    }
  }

  getSectorColorClass(sector: Sector): string {
    const sectorIndex = this.room.sectors.indexOf(sector);
    return ['blue-sector', 'yellow-sector', 'green-sector'][sectorIndex % 3];
  }


}
