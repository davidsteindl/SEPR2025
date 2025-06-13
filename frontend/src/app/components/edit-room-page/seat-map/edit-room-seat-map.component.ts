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

  ngOnChanges(changes: SimpleChanges) {
    if (changes["room"]) {
      this.buildGrid();
      this.computeSectorColors();
      this.splitSectors();
      this.allSectors = this.room.sectors;
    }
  }

  private buildGrid() {
    this.gridTemplateColumns = `repeat(${this.room.xSize}, 40px)`;
    this.gridTemplateRows = `repeat(${this.room.ySize}, 40px)`;
  }

  private computeSectorColors() {
    this.sectorColorMap = {};
    (this.room.sectors || []).forEach((sec) => {
      const hue = Math.floor(Math.random() * 360);
      this.sectorColorMap[sec.id] = `hsl(${hue},60%,75%)`;
    });
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

  openPopover(seat: Seat, pop: NgbPopover) {
    // Initialize the selected sector for this seat when popover opens
    this.selectedSectorIdMap[seat.id] = seat.sectorId ?? null;
    if (!pop.isOpen()) {
      pop.open({ seat });
    }
  }

  assignSeat(seat: Seat, sectorId: number | null, pop: NgbPopover) {
    seat.deleted = false;
    seat.sectorId = sectorId;
    pop.close();
  }

  deleteSeat(seat: Seat, pop: NgbPopover) {
    seat.deleted = true;
    pop.close();
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
}
