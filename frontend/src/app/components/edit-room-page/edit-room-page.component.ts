// edit-room-page.component.ts
import { Component, OnInit, ViewChild } from "@angular/core";
import { ActivatedRoute, ParamMap } from "@angular/router";
import { RoomService } from "src/app/services/room.service";
import { Room } from "src/app/dtos/room";
import { Sector, SectorType } from "src/app/dtos/sector";
import { NgbModal, NgbModalRef } from "@ng-bootstrap/ng-bootstrap";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ReactiveFormsModule } from "@angular/forms";
import { EditRoomSeatMapComponent } from "./seat-map/edit-room-seat-map.component";
import { CommonModule } from "@angular/common";
import { U } from "@angular/common/common_module.d-Qx8B6pmN";

@Component({
  selector: "app-edit-room-page",
  standalone: true,
  templateUrl: "./edit-room-page.component.html",
  styleUrls: ["./edit-room-page.component.scss"],
  imports: [CommonModule, ReactiveFormsModule, EditRoomSeatMapComponent],
})
export class EditRoomPageComponent implements OnInit {
  room!: Room;
  loading = true;

  // form for creating/editing a sector
  sectorForm!: FormGroup;
  private editingModalRef!: NgbModalRef;
  editingSector?: Sector;

  @ViewChild(EditRoomSeatMapComponent) seatMap!: EditRoomSeatMapComponent;

  constructor(
    private route: ActivatedRoute,
    private roomService: RoomService,
    private modalService: NgbModal,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params: ParamMap) => {
      const id = +params.get("id")!;
      this.roomService.getRoomById(id).subscribe((r) => {
        this.room = r;
        this.loading = false;
      });
    });
  }

  openNewSectorModal(template: any) {
    this.editingSector = undefined;
    this.buildSectorForm();
    this.editingModalRef = this.modalService.open(template, { centered: true });
  }

  openEditSectorModal(template: any, sector: Sector) {
    this.editingSector = sector;
    this.buildSectorForm(sector);
    this.editingModalRef = this.modalService.open(template, { centered: true });
  }

  private buildSectorForm(sector?: Sector) {
    this.sectorForm = this.fb.group({
      id: [sector?.id ?? null],
      type: [sector?.type ?? SectorType.NORMAL, Validators.required],
      price: [
        {
          value: sector?.price ?? 0,
          disabled: sector?.type === SectorType.STAGE,
        },
        Validators.min(0),
      ],
      capacity: [
        {
          value: sector?.capacity ?? null,
          disabled: sector?.type !== SectorType.STANDING,
        },
        Validators.min(1),
      ],
    });
    // react to type changes
    this.sectorForm.get("type")!.valueChanges.subscribe((t: SectorType) => {
      if (t === SectorType.STAGE) {
        this.sectorForm.get("price")!.disable();
        this.sectorForm.get("capacity")!.disable();
      } else if (t === SectorType.NORMAL) {
        this.sectorForm.get("price")!.enable();
        this.sectorForm.get("capacity")!.disable();
      } else {
        // STANDING
        this.sectorForm.get("price")!.enable();
        this.sectorForm.get("capacity")!.enable();
      }
    });
  }

  saveSector() {
    const val = this.sectorForm.getRawValue();
    // normalize stage
    if (val.type === SectorType.STAGE) {
      val.price = 0;
      val.capacity = null;
    }
    if (this.editingSector) {
      Object.assign(this.editingSector, val);
      // For editing, just update and call saveLayout
      this.saveLayout(() => this.editingModalRef.close());
    } else {
      // Remove id for new sector (let backend assign)
      delete val.id;
      this.room.sectors.push(val as Sector);
      // Save layout and replace room with backend response
      this.saveLayout(() => this.editingModalRef.close());
    }
    // No longer close modal here; handled in saveLayout callback
  }

  saveLayout(afterSuccess?: () => void) {
    // Removed unassigned seat check
    console.log("ROOM: ", this.room);
    this.roomService.edit(this.room).subscribe({
      next: (updatedRoom) => {
        console.log("updated room ", updatedRoom);
        this.room = updatedRoom;
        if (this.seatMap) {
          this.seatMap.refreshSectors();
        }
        if (afterSuccess) afterSuccess();
        console.log("Layout saved successfully");
      },
      error: (err) => {
        console.error("Error saving layout:", err);
      },
    });
  }

  deleteSector(sector: Sector) {
    // unassign seats
    this.room.seats.forEach((seat) => {
      if (seat.sectorId === sector.id) {
        seat.sectorId = null;
      }
    });
    this.room.sectors = this.room.sectors.filter((s) => s.id !== sector.id);
  }

  // Expose sectorColorMap for template
  get sectorColorMap() {
    return this.seatMap?.sectorColorMap || {};
  }

  SectorType = SectorType;
}
