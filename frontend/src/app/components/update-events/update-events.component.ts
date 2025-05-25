import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NgForOf } from '@angular/common';
import { EventService } from '../../services/event.service';
import { Event } from '../../dtos/event';

@Component({
  selector: 'app-update-events',
  standalone: true,
  imports: [
    CommonModule,
    NgForOf,
    RouterLink
  ],
  templateUrl: './update-events.component.html',
  styleUrl: './update-events.component.scss'
})
export class UpdateEventsComponent implements OnInit {
  events: Event[] = [];
  loading = false;
  error: string = null;

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.eventService.getAll().subscribe({
      next: evs => {
        this.events = evs;
        this.loading = false;
      },
      error: err => {
        this.error = 'Konnte Events nicht laden';
        console.error(err);
        this.loading = false;
      }
    });
  }
}
