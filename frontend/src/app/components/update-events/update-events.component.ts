import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NgForOf } from '@angular/common';
import { EventService } from '../../services/event.service';
import { Event } from '../../dtos/event';
import { ToastrService } from 'ngx-toastr';
import { ErrorFormatterService } from '../../services/error-formatter.service';
import {Page} from "../../dtos/page";
@Component({
  selector: 'app-update-events',
  standalone: true,
  imports: [
    CommonModule,
    NgForOf,
    RouterLink
  ],
  templateUrl: './update-events.component.html',
  styleUrls: ['./update-events.component.scss']
})
export class UpdateEventsComponent implements OnInit {
  error = false;
  errorMessage = '';

  eventsPage?: Page<Event>;
  eventsCurrentPage = 0;
  eventsPageSize = 10;
  eventsLoading = false;
  eventsTriggered = false;

  constructor(
    private eventService: EventService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) { }

  ngOnInit(): void {
    this.loadAllEvents();
  }

  loadAllEvents(page: number = 0): void {
    this.eventsLoading = true;
    this.eventsTriggered = true;
    this.error = false;

    this.eventService.getPaginatedEvents(page, this.eventsPageSize).subscribe({
      next: pageResult => {
        this.eventsPage = pageResult;
        this.eventsCurrentPage = page;
        this.eventsLoading = false;
      },
      error: err => {
        this.eventsPage = undefined;
        this.eventsLoading = false;
        this.eventsTriggered = false;
        console.error('Error loading events:', err);
        this.notification.error(
          this.errorFormatter.format(err),
          'Error while fetching events',
          { enableHtml: true, timeOut: 8000 }
        );
      }
    });
  }
}
