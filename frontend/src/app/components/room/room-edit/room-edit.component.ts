import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {RoomService} from "../../../services/room.service";
import {Room} from "../../../dtos/room";
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {StandingSector} from "../../../dtos/standing-sector";
import {SeatedSector} from "../../../dtos/seated-sector";
import {Seat} from "../../../dtos/seat";
import {Sector} from "../../../dtos/sector";
import {FormsModule} from "@angular/forms";
import {SectorType} from "../../../dtos/sector-type";

@Component({
  selector: 'app-room',
  imports: [
    NgIf,
    NgClass,
    NgForOf,
    FormsModule
  ],
  templateUrl: './room-edit.component.html',
  styleUrl: './room-edit.component.scss'
})
export class RoomEditComponent implements OnInit {

  room: Room | null = null;


  selectedSeat: Seat;
  selectedSector: Sector;
  globalRow: number;
  rowsForSector: number;
  seatsForRow: number;
  capacity: number;
  sectorType = SectorType;

  addingNewSector = false;
  newSectorType: SectorType = this.sectorType.SEATED;
  newSectorRows: number;
  newSectorSeatsForRow: number;
  newSectorPrice: number;
  newSectorCapacity: number;

  constructor(private route: ActivatedRoute,
              private roomService: RoomService,
              private notification: ToastrService,
              private errorFormatter: ErrorFormatterService,
              private router: Router
  ) {
  }

  ngOnInit(): void {
    this.getRoomById();
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

  isSeat(sector: SeatedSector, row: number, col: number): boolean {
    const seat = sector.rows.find(s => s.rowNumber === row && s.columnNumber === col && !s.deleted);
    return !!seat;
  }

  isSelected(sector: SeatedSector, row: number, col: number): boolean {
    return this.selectedSeat &&
      this.selectedSeat.rowNumber === row &&
      this.selectedSeat.columnNumber === col &&
      this.selectedSector?.id === sector.id;
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
      this.selectedSector = sector;
      this.getSeatInfo(seat, sector);
      console.log(`Clicked seat ${this.globalRow}${this.toColumnLetter(seat.columnNumber)}`);
    }
  }

  getSeatInfo(seat: Seat, sector: SeatedSector): void {
    const sectorIndex = this.seatedSectors.findIndex(s => s.id === sector.id);
    this.globalRow = seat.rowNumber + this.getRowOffset(sectorIndex);
  }

  getSectorColorClass(sector: Sector): string {
    const sectorIndex = this.room.sectors.indexOf(sector);
    return ['blue-sector', 'yellow-sector', 'green-sector'][sectorIndex % 3];
  }

  isSelectedSector(sector: Sector): boolean {
    if (this.selectedSeat) return false;
    return this.selectedSector?.id === sector.id;
  }


  onSectorClick(sector: SeatedSector): void {
    this.selectedSeat = null;
    this.selectedSector = sector;
    this.rowsForSector = Math.max(...sector.rows.map(seat => seat.rowNumber));
    this.seatsForRow = Math.max(...sector.rows.map(seat => seat.columnNumber));


    console.log(`Clicked sector ${sector.id}`);
  }

  onStandingSectorClick(sector: StandingSector): void {
    this.selectedSeat = null;
    this.selectedSector = sector;
    this.capacity = sector.capacity;

    console.log(`Clicked sector ${sector.id}`);
  }

  resetSector(): void {
    this.selectedSector = null;
  }

  addSector(): void {
    this.addingNewSector = true;
    this.selectedSeat = null;
    this.selectedSector = null;
  }

  cancelAddSector(): void {
    this.addingNewSector = false;
  }

  deleteSeat(): void {
    this.selectedSeat.deleted = true;
    this.selectedSeat = null;
    this.selectedSector = null;
  }

  getSelectedSectorIndex(): number | null {
    if (!this.selectedSector || !this.room) return null;
    return this.room.sectors.indexOf(this.selectedSector) + 1;
  }


  unClickSeat(): void {
    this.selectedSeat = null;
    this.selectedSector = null;
  }

  validatePrice(): void {
    if (this.selectedSector == null) {
      return;
    }
    if (this.selectedSector.price < 10) {
      this.selectedSector.price = 10;
    } else if (this.selectedSector.price > 200) {
      this.selectedSector.price = 200;
    }
  }

  edit(): void {
    this.roomService.edit(this.room).subscribe({
      next: (response) => {
        if (response) {
          this.notification.success(`Room ${response.name} edited successfully!`, 'Success', {
            enableHtml: true,
            timeOut: 8000,
          });
          this.router.navigate(['/rooms', this.room.id, 'overview']);
        }
      },
      error: (err) => {
        console.error('Error while editing room:', err);
        this.notification.error(this.errorFormatter.format(err), 'Error while editing room', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }

}
