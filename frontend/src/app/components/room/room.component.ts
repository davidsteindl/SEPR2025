import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {RoomService} from "../../services/room.service";
import {Location} from "../../dtos/location";
import {Room} from "../../dtos/room";
import {NgClass, NgForOf, NgIf, NgTemplateOutlet} from "@angular/common";
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
export class RoomComponent implements OnInit {

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

  testSeat5: Seat = {
    id: 5,
    rowNumber: 1,
    columnNumber: 3,
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
    rows: [this.testSeat1, this.testSeat2, this.testSeat4, this.testSeat5],
    price: 10,
    room: null,
    type: SectorType.SEATED
  }
  testSector3: SeatedSector = {
    id: 3,
    rows: [this.testSeat1, this.testSeat2, this.testSeat3, this.testSeat4],
    price: 10,
    room: null,
    type: SectorType.SEATED
  }

  testSector4: StandingSector = {
    id: 4,
    capacity: 50,
    price: 10,
    room: null,
    type: SectorType.STANDING
  }


  testRoom: Room = {
    id: 2,
    sectors: [this.testSector1, this.testSector2, this.testSector3, this.testSector4],
    name: "Testroom",
    eventLocation: this.testLocation
  }

  selectedSeat: Seat;
  globalRow: number;


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
      error: err => {
        this.notification.error(this.errorFormatter.format(err), 'Loading events failed', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }

  get seatedSectors(): SeatedSector[] {
    return this.room.sectors.filter(s => this.asSeatedSector(s)) as SeatedSector[];
  }

  get standingSectors(): StandingSector[] {
    return this.room.sectors.filter(s => this.asStandingSector(s)) as StandingSector[];
  }

  asSeatedSector(sector: Sector): SeatedSector | undefined {
    if ((sector as SeatedSector).rows !== undefined) {
      return sector as SeatedSector;
    }
    return undefined;
  }

  asStandingSector(sector: Sector): StandingSector | undefined {
    if ((sector as StandingSector).capacity !== undefined) {
      return sector as StandingSector;
    }
    return undefined;
  }

  getMaxRows(sector: SeatedSector): number[] {
    const max = Math.max(...sector.rows.map(seat => seat.rowNumber));
    return Array.from({length: max}, (_, i) => i + 1);
  }

  isSeat(sector: SeatedSector, row: number, col: number) : boolean {
    const seat = sector.rows.find(s => s.rowNumber === row && s.columnNumber === col && !s.deleted);
    if (seat) return true;
    else return false
  }


  get globalColumns(): number[] {
    let maxCol = 0;
    for (const sector of this.seatedSectors) {
      for (const seat of sector.rows) {
        if (seat.columnNumber > maxCol) {
          maxCol = seat.columnNumber;
        }
      }
    }
    return Array.from({length: maxCol}, (_, i) => i + 1);
  }


  getRowOffset(index: number): number {
    return this.seatedSectors
      .slice(0, index)
      .reduce((acc, s) => acc + this.getMaxRows(s).length, 0);
  }

  toColumnLetter(col: number): string {
    return String.fromCharCode(96 + col); // 1 -> 'a', 2 -> 'b', ...
  }


  onSeatClick(sector: SeatedSector, row: number, col: number) {
    const seat = sector.rows.find(s => s.rowNumber === row && s.columnNumber === col && !s.deleted);
    if (seat) {
      this.selectedSeat = seat;
      const sectorIndex = this.seatedSectors.findIndex(s => s.id === sector.id);
      this.globalRow = seat.rowNumber + this.getRowOffset(sectorIndex);
      console.log(`Clicked seat ${this.globalRow}${this.toColumnLetter(seat.columnNumber)}`);
    }
  }

  getSectorColorClass(sector: Sector): string {
    const sectorIndex = this.room.sectors.indexOf(sector);
    return ['blue-sector', 'yellow-sector', 'green-sector'][sectorIndex % 3];
  }


  onSectorClick(sector: Sector): void {
    console.log(`Clicked sector ${sector.id}`);
  }

}
