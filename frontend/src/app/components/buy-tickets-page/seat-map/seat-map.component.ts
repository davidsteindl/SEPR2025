import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  TemplateRef,
  ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModule, NgbPopover } from '@ng-bootstrap/ng-bootstrap';
import { Room } from 'src/app/dtos/room';
import { Sector } from 'src/app/dtos/sector';
import { Seat } from 'src/app/dtos/seat';
import { PaymentItem } from 'src/app/dtos/payment-item';

@Component({
  selector: 'app-seat-map',
  standalone: true,
  imports: [CommonModule, FormsModule, NgbModule],
  templateUrl: './seat-map.component.html',
  styleUrls: ['./seat-map.component.scss']
})
export class SeatMapComponent implements OnInit, OnChanges {
  @Input() room!: Room;
  @Input() showId!: number;
  @Input() eventName!: string;
  @Input() cartItems: PaymentItem[] = [];

  @Output() seatSelected = new EventEmitter<PaymentItem>();

  @ViewChild('seatPopoverTpl', { static: true }) seatPopoverTpl!: TemplateRef<any>;
  @ViewChild('standingPopoverTpl', { static: true }) standingPopoverTpl!: TemplateRef<any>;

  // map for sectorId -> random color string 
  sectorColorMap: { [sectorId: number]: string } = {};


  seatedSectors: Sector[] = [];
  standingSectors: Sector[] = [];


  standingRects: Array<{
    sector: Sector;
    minRow: number;
    maxRow: number;
    minCol: number;
    maxCol: number;
  }> = [];
  // Lookup maps
  //  - seatById: maps seatId to Seat object
  //  - sectorOfSeat: maps seatId to Sector object (for seated seats)
  private seatById: { [id: number]: Seat } = {};
  private sectorOfSeat: { [seatId: number]: Sector } = {};


  gridTemplateColumns = '';
  gridTemplateRows = '';


  standingQuantity: { [sectorId: number]: number } = {};


  selectedSeatIdSet = new Set<number>();
  selectedStandingSectorSet = new Set<number>();

  constructor() {}

  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['room'] && this.room) {
      this.buildGrid();
      this.computeSectorColors();
      this.splitSectors();
      this.computeStandingRectangles();
      this.buildCartSelectionSets();
    }
    if (changes['cartItems']) {
      this.buildCartSelectionSets();
    }
  }

  private buildGrid(): void {
    // Set grid template columns and rows based on room size
    this.gridTemplateColumns = `repeat(${this.room.xSize}, 40px)`;
    this.gridTemplateRows = `repeat(${this.room.ySize}, 40px)`;

    // Build seatById lookup
    this.seatById = {};
    for (const s of this.room.seats) {
      this.seatById[s.id] = s;
    }
  }

  private computeSectorColors(): void {
  
    // Generate random colors for each sector
    this.sectorColorMap = {};
    for (const sec of this.room.sectors) {
      const hue = Math.floor(Math.random() * 360);
      this.sectorColorMap[sec.id] = `hsl(${hue}, 60%, 75%)`;
    }
  }

  private splitSectors(): void {
    this.seatedSectors = [];
    this.standingSectors = [];

    for (const sec of this.room.sectors) {
      if (sec.capacity != null && sec.capacity !== undefined) {
        this.standingSectors.push(sec);
      } else {
        this.seatedSectors.push(sec);
      }
      // Initialize seat lookup for seated sectors
      for (const seat of sec.seats) {
        this.sectorOfSeat[seat.id] = sec;
      }
    }
  }

  private computeStandingRectangles(): void {
    this.standingRects = [];
    for (const sec of this.standingSectors) {
      // get all rowNumbers and columnNumbers of seats in this sector
      const rows = sec.seats.map((st) => st.rowNumber);
      const cols = sec.seats.map((st) => st.columnNumber);
      const minRow = Math.min(...rows);
      const maxRow = Math.max(...rows);
      const minCol = Math.min(...cols);
      const maxCol = Math.max(...cols);
      this.standingRects.push({ sector: sec, minRow, maxRow, minCol, maxCol });
      // initialize default quantity = 1
      this.standingQuantity[sec.id] = 1;
    }
  }

  private buildCartSelectionSets(): void {
    // Clear existing sets
    this.selectedSeatIdSet.clear();
    this.selectedStandingSectorSet.clear();

    for (const it of this.cartItems) {
      if (it.type === 'SEATED' && it.seatId != null) {
        this.selectedSeatIdSet.add(it.seatId);
      }
      if (it.type === 'STANDING' && it.sectorId != null) {
        this.selectedStandingSectorSet.add(it.sectorId);
      }
    }
  }

  // Called when user clicks a seated seat circle
  openSeatPopover(seat: Seat, sector: Sector, pop: NgbPopover) {
    if (!seat.deleted && seat.available !== false) {
      pop.toggle();
    }
  }

  // Called when user presses “+ Add” in the seated popover
  addSeatedToCart(seat: Seat, sector: Sector, pop: NgbPopover) {
    const item: PaymentItem = {
      eventName: this.eventName,
      type: 'SEATED',
      price: sector.price,
      sectorId: sector.id,
      seatId: seat.id,
      rowNumber: seat.rowNumber,
      columnNumber: seat.columnNumber,
      showId: this.showId
    };
    this.seatSelected.emit(item);
    pop.close();
  }

  // Called when user clicks a standing‐sector rectangle
  openStandingPopover(sec: Sector, pop: NgbPopover) {
    // if ANY seat in this sector is unavailable, gray out, else toggle
    const allDeleted = sec.seats.every((st) => st.deleted);
    if (!allDeleted) {
      pop.toggle();
    }
  }

  // Called when user presses ADD in the standing popover
  addStandingToCart(sec: Sector, pop: NgbPopover) {
    const qty = this.standingQuantity[sec.id] || 1;
    const item: PaymentItem = {
      eventName: this.eventName,
      type: 'STANDING',
      price: sec.price,
      sectorId: sec.id,
      quantity: qty,
      showId: this.showId
    };
    this.seatSelected.emit(item);
    pop.close();
  }

  // check whether a given seated Seat is already in cart
  isSeatSelected(seat: Seat): boolean {
    return this.selectedSeatIdSet.has(seat.id);
  }

  // check whether a standing sector is in cart
  isStandingSelected(sec: Sector): boolean {
    return this.selectedStandingSectorSet.has(sec.id);
  }
}
