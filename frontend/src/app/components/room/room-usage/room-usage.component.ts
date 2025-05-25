import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {ShowService} from "../../../services/show.service";
import {Room} from "../../../dtos/room";
import {NgClass, NgForOf, NgIf, NgStyle} from "@angular/common";
import {StandingSector} from "../../../dtos/standing-sector";
import {SeatedSector} from "../../../dtos/seated-sector";
import {Seat} from "../../../dtos/seat";
import {Sector} from "../../../dtos/sector";
import {AuthService} from "../../../services/auth.service";

@Component({
  selector: 'app-room-usage',
  imports: [
    NgIf,
    NgClass,
    RouterLink,
    NgForOf,
    NgStyle
  ],
  templateUrl: './room-usage.component.html',
  styleUrl: './room-usage.component.scss'
})
export class RoomUsageComponent implements OnInit {

  room: Room | null = null;
  isAdmin: boolean;

  selectedSeats: Seat[] = [];
  selectedStandingTickets: { [sectorId: number]: number } = {};

  uniquePrices: number[] = [];
  priceColorMap: { [price: number]: string } = {};
  distinctColors: string[] = [
    '#1abc9c', '#3498db', '#9b59b6', '#e67e22', '#e74c3c',
    '#2ecc71', '#f1c40f', '#34495e', '#7f8c8d', '#8e44ad',
    '#16a085', '#c0392b', '#d35400', '#27ae60', '#2980b9'
  ];

  constructor(private route: ActivatedRoute,
              private showService: ShowService,
              private notification: ToastrService,
              private authService: AuthService,
              private errorFormatter: ErrorFormatterService
  ) {
  }

  ngOnInit(): void {
    this.getRoomByShowId();
    this.isAdmin = this.authService.getUserRole() === 'ADMIN';
    console.log(this.room);
  }

  generatePriceColorMap(): void {
    if (!this.room) return;

    this.uniquePrices = Array.from(
      new Set(this.room.sectors.map(sector => sector.price))
    ).sort((a, b) => a - b);

    this.uniquePrices.forEach((price, index) => {
      this.priceColorMap[price] = this.distinctColors[index % this.distinctColors.length];
    });
  }


  getSectorColorByPrice(sector: Sector): string {
    return this.priceColorMap[sector.price] || '#ffffff';
  }


  getRoomByShowId(): void {
    const showId = Number(this.route.snapshot.paramMap.get('id'));

    this.showService.getRoomUsage(showId).subscribe({
      next: room => {
        this.room = room;
        this.generatePriceColorMap();
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

  getSeat(sector: SeatedSector, row: number, col: number): Seat | undefined {
    return sector.rows.find(
      s => s.rowNumber === row && s.columnNumber === col && !s.deleted
    );
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

  toggleSeatSelection(seat: Seat): void {
    const index = this.selectedSeats.findIndex(
      s => s.rowNumber === seat.rowNumber &&
        s.columnNumber === seat.columnNumber &&
        s.id === seat.id
    );

    if (index !== -1) {
      this.selectedSeats.splice(index, 1);
    } else {
      this.selectedSeats.push(seat);
    }

    console.log('Selected seats:', this.selectedSeats);
  }


  getSectorColorClass(sector: Sector): string {
    const sectorIndex = this.room.sectors.indexOf(sector);
    return ['blue-sector', 'yellow-sector', 'green-sector'][sectorIndex % 3];
  }


  onSectorClick(sector: Sector): void {
    console.log(`Clicked sector ${sector.id}`);
  }

  hasAnySelection(): boolean {
    return this.selectedSeats.length > 0 || Object.values(this.selectedStandingTickets).some(v => v > 0);
  }

  increaseStandingTickets(sector: StandingSector): void {
    const current = this.selectedStandingTickets[sector.id] || 0;
    if (current < sector.capacity) {
      this.selectedStandingTickets[sector.id] = current + 1;
    }
  }

  decreaseStandingTickets(sector: StandingSector): void {
    const current = this.selectedStandingTickets[sector.id] || 0;
    if (current > 0) {
      this.selectedStandingTickets[sector.id] = current - 1;
    }
  }

  buyTickets(): void {
    console.log('Buying seated seats:', this.selectedSeats);
    console.log('Buying standing tickets:', this.selectedStandingTickets);
    // TODO: Call service to submit both
  }

  reserveSeats(): void {
    console.log('Reserving seated seats:', this.selectedSeats);
    console.log('Reserving standing tickets:', this.selectedStandingTickets);
    // TODO: Call service to submit both
  }
}
