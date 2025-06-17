// edit-room-seat-map.component.ts
import { Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { Room } from "src/app/dtos/room";
import { Sector, SectorType } from "src/app/dtos/sector";
import { Seat } from "src/app/dtos/seat";
import { NgbPopover } from "@ng-bootstrap/ng-bootstrap";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { NgbModule } from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: "app-edit-room-seat-map",
  standalone: true,
  imports: [CommonModule, FormsModule, NgbModule],
  templateUrl: "./edit-room-seat-map.component.html",
  styleUrls: ["./edit-room-seat-map.component.scss"],
})
export class EditRoomSeatMapComponent implements OnChanges {
  @Input() room!: Room;

  gridTemplateColumns = "";
  gridTemplateRows = "";
  // color map for sectors
  sectorColorMap: Record<number, string> = {};
  seatedSectors: Sector[] = [];
  stageSectors: Sector[] = [];
  allSectors: Sector[] = [];

  // Track selected sector for each seat popover
  selectedSectorIdMap: { [seatId: number]: number | null } = {};

  // Multi-seat selection
  selectedSeats: Seat[] = [];

  // Track the currently open popover and seat
  private lastPopover: NgbPopover | null = null;
  private lastPopoverSeatId: number | null = null;

  ngOnChanges(changes: SimpleChanges) {
    if (changes["room"]) {
      this.buildGrid();
      this.computeSectorColors();
      this.splitSectors();
      this.allSectors = this.room.sectors;
      this.selectedSeats = [];
    }
  }

  private buildGrid() {
    this.gridTemplateColumns = `repeat(${this.room.xSize}, 40px)`;
    this.gridTemplateRows = `repeat(${this.room.ySize}, 40px)`;
  }

  private computeSectorColors() {
    this.sectorColorMap = {};
    (this.room.sectors || []).forEach((sec) => {
      // Deterministic color based on sector id
      const hue = this.hashToHue(sec.id);
      this.sectorColorMap[sec.id] = `hsl(${hue},60%,75%)`;
    });
  }

  // Simple hash function to map sector id to a hue (0-359)
  private hashToHue(id: number): number {
    let hash = id;
    hash = ((hash >> 16) ^ hash) * 0x45d9f3b;
    hash = ((hash >> 16) ^ hash) * 0x45d9f3b;
    hash = (hash >> 16) ^ hash;
    return Math.abs(hash) % 360;
  }

  private splitSectors() {
    this.stageSectors = [];
    this.seatedSectors = [];

    this.room.sectors.forEach((sec) => {
      if (sec.type === SectorType.STAGE) {
        this.stageSectors.push(sec);
      } else {
        this.seatedSectors.push(sec);
      }
    });
  }

  // Multi-select logic: ctrl+click toggles selection, normal click selects one
  onSeatClick(seat: Seat, event: MouseEvent) {
    if (event.ctrlKey) {
      const idx = this.selectedSeats.findIndex((s) => s.id === seat.id);
      if (idx >= 0) {
        this.selectedSeats.splice(idx, 1);
      } else {
        this.selectedSeats.push(seat);
        // Move popover to this seat
        if (this.lastPopover && this.lastPopoverSeatId !== seat.id) {
          this.lastPopover.close();
        }
        if (this.lastPopover && !this.lastPopover.isOpen()) {
          this.lastPopover.open({ seat });
        }
        this.lastPopoverSeatId = seat.id;
      }
    } else {
      this.selectedSeats = [seat];
    }
  }

  openPopover(seat: Seat, pop: NgbPopover, event?: MouseEvent) {
    // Multi-select: only open popover if not ctrl+click
    if (event && event.ctrlKey) {
      this.onSeatClick(seat, event);
      // Move popover to this seat
      if (this.lastPopover && this.lastPopover !== pop) {
        this.lastPopover.close();
      }
      if (!pop.isOpen()) {
        pop.open({ seat });
      }
      this.lastPopover = pop;
      this.lastPopoverSeatId = seat.id;
      return;
    }
    this.selectedSectorIdMap[seat.id] = seat.sectorId ?? null;
    this.selectedSeats = [seat];
    if (this.lastPopover && this.lastPopover !== pop) {
      this.lastPopover.close();
    }
    if (!pop.isOpen()) {
      pop.open({ seat });
    }
    this.lastPopover = pop;
    this.lastPopoverSeatId = seat.id;
  }

  assignSeat(seat: Seat, sectorId: number | null, pop: NgbPopover) {
    const seatsToAssign =
      this.selectedSeats.length > 0 ? this.selectedSeats : [seat];
    seatsToAssign.forEach((s) => {
      s.deleted = false;
      s.sectorId = sectorId;
    });
    this.selectedSeats = [];
    if (this.lastPopover) this.lastPopover.close();
    this.lastPopover = null;
    this.lastPopoverSeatId = null;
  }

  deleteSeat(seat: Seat, pop: NgbPopover) {
    const seatsToDelete =
      this.selectedSeats.length > 0 ? this.selectedSeats : [seat];
    seatsToDelete.forEach((s) => {
      s.sectorId = null; // unassign sector before deleting
      s.deleted = true;
    });
    this.selectedSeats = [];
    if (this.lastPopover) this.lastPopover.close();
    this.lastPopover = null;
    this.lastPopoverSeatId = null;
  }

  /**
   * Returns all seats for a given sectorId
   */
  getSeatsForSector(sectorId: number): Seat[] {
    return (this.room?.seats || []).filter((s) => s.sectorId === sectorId);
  }

  /**
   * Call this to refresh sector lists after room.sectors changes
   */
  public refreshSectors() {
    this.splitSectors();
    this.computeSectorColors();
    this.allSectors = this.room.sectors;
  }

  /**
   * Returns the type of the sector for a given sectorId
   */
  getSectorType(sectorId: number | null | undefined): string | null {
    if (sectorId == null) return null;
    const sector = this.room.sectors.find((s) => s.id === sectorId);
    return sector ? sector.type : null;
  }

  isSeatSelected(seat: Seat): boolean {
    return this.selectedSeats.some((s) => s.id === seat.id);
  }
}
