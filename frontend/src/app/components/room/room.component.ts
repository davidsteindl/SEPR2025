import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {RoomService} from "../../services/room.service";
import {Room} from "../../dtos/room";
import {NgClass, NgForOf, NgIf, NgTemplateOutlet} from "@angular/common";
import {StandingSector} from "../../dtos/standing-sector";
import {SeatedSector} from "../../dtos/seated-sector";
import {Seat} from "../../dtos/seat";
import {Sector} from "../../dtos/sector";
import {AuthService} from "../../services/auth.service";

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
  isAdmin: boolean;

  selectedSeat: Seat;
  globalRow: number;


  constructor(private route: ActivatedRoute,
              private roomService: RoomService,
              private notification: ToastrService,
              private authService: AuthService,
              private errorFormatter: ErrorFormatterService
  ) {
  }

  ngOnInit(): void {
    this.getRoomById();
    this.isAdmin = this.authService.getUserRole() === 'ADMIN';
    console.log(this.room);
  }

  getRoomById(): void {
    const roomId = Number(this.route.snapshot.paramMap.get('id'));

    this.roomService.getRoomById(roomId).subscribe({
      next: room => {
        this.room = room;

      },
      error: err => {
        this.notification.error(this.errorFormatter.format(err), 'Loading room failed', {
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
    return this.room.sectors
      .slice(0, index)
      .filter(s => this.asSeatedSector(s))
      .reduce((acc, s) => {
        const seated = this.asSeatedSector(s);
        return acc + (seated ? this.getMaxRows(seated).length : 0);
      }, 0);
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
