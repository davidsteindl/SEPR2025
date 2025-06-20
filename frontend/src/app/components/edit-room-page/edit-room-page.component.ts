// edit-room-page.component.ts
import { Component, OnInit, ViewChild } from "@angular/core";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import { RoomService } from "src/app/services/room.service";
import { Room } from "src/app/dtos/room";
import { Sector, SectorType } from "src/app/dtos/sector";
import { NgbModal, NgbModalRef } from "@ng-bootstrap/ng-bootstrap";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ReactiveFormsModule } from "@angular/forms";
import { EditRoomSeatMapComponent } from "./seat-map/edit-room-seat-map.component";
import { CommonModule } from "@angular/common";
import { ToastrService } from "ngx-toastr";

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

  showAdminReturn = false;
  showConfirmExit = false;
  private initialRoomState!: string;

  // form for creating/editing a sector
  sectorForm!: FormGroup;
  private editingModalRef!: NgbModalRef;
  editingSector?: Sector;

  @ViewChild(EditRoomSeatMapComponent) seatMap!: EditRoomSeatMapComponent;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private roomService: RoomService,
    private modalService: NgbModal,
    private fb: FormBuilder,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.showAdminReturn = this.route.snapshot.queryParamMap.get('fromAdmin') === 'true';

    this.route.paramMap.subscribe((params: ParamMap) => {
      const id = +params.get("id")!;
      this.roomService.getRoomById(id).subscribe((r) => {
        console.log("Room loaded: ", r);
        this.room = r;
        this.initialRoomState = JSON.stringify(r);
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
      val.price = null;
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
    console.log("ROOM to be saved: ", this.room);
    this.roomService.edit(this.room).subscribe({
      next: (updatedRoom) => {
        console.log("updated room ", updatedRoom);
        this.room = updatedRoom;
        if (this.seatMap) {
          this.seatMap.refreshSectors();
        }
        if (afterSuccess) afterSuccess();
        this.toastr.success("Room saved successfully!");
        console.log("Layout saved successfully");
        this.router.navigate(['/update-rooms'], {
          queryParams: { fromAdmin: this.showAdminReturn }
        });
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

  getSectorColor(id: number): string {
    return this.seatMap?.sectorColorMap[id] || "#ccc";
  }

  SectorType = SectorType;

  onBackToUpdateRoomsClick(): void {
    if (this.isUnchanged()) {
      this.router.navigate(['/update-rooms'], {
        queryParams: { fromAdmin: this.showAdminReturn }
      });
    } else {
      this.showConfirmExit = true;
    }
  }

  private isUnchanged(): boolean {
    return JSON.stringify(this.room) === this.initialRoomState;
  }

  stay(): void {
    this.showConfirmExit = false;
  }

  exit(): void {
    this.showConfirmExit = false;
    this.router.navigate(['/update-rooms'], {
      queryParams: { fromAdmin: this.showAdminReturn }
    });
  }
}
