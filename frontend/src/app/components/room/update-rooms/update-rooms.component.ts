import {Component, OnInit} from '@angular/core';
import {CommonModule, NgForOf} from "@angular/common";
import {ActivatedRoute, RouterLink} from "@angular/router";
import {Page} from "../../../dtos/page";
import {RoomPageDto} from "../../../dtos/room";
import {RoomService} from "../../../services/room.service";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";

@Component({
  selector: 'app-update-rooms',
  standalone: true,
  imports: [CommonModule, NgForOf, RouterLink],
  templateUrl: './update-rooms.component.html',
  styleUrls: ['./update-rooms.component.scss']
})
export class UpdateRoomsComponent implements OnInit {
  roomsPage?: Page<RoomPageDto>;
  roomsCurrentPage = 0;
  roomsPageSize = 10;
  roomsLoading = false;
  roomsTriggered = false;

  showBackButton = false;

  constructor(
    private roomService: RoomService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.showBackButton = this.route.snapshot.queryParamMap.get('fromAdmin') === 'true';
    this.loadAllRooms();
  }

  loadAllRooms(page: number = 0): void {
    this.roomsLoading = true;
    this.roomsTriggered = true;

    this.roomService.getPaginatedRooms(page, this.roomsPageSize)
      .subscribe({
        next: pageResult => {
          this.roomsPage = pageResult;
          this.roomsCurrentPage = pageResult.number;
          this.roomsLoading = false;
        },
        error: err => {
          this.roomsPage = undefined;
          this.roomsLoading = false;
          console.error('Error loading rooms:', err);
          this.notification.error(
            this.errorFormatter.format(err),
            'Error while fetching rooms',
            { enableHtml: true, timeOut: 8000 }
          );
        }
      });
  }
}
