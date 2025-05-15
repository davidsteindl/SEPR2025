import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event } from '../../dtos/event';
import { Show } from '../../dtos/show';
import { EventWithShows } from '../../dtos/event';
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-event-overview',
  standalone: true,
  templateUrl: './event-overview.component.html',
  imports: [
    NgIf,
    NgForOf
  ]
})
export class EventOverviewComponent implements OnInit {
  event: Event | null = null;
  shows: Show[] = [];

  constructor(private route: ActivatedRoute, private eventService: EventService) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.eventService.getEventWithShows(id).subscribe({
      next: (data: EventWithShows) => {
        this.event = data.event;
        this.shows = data.shows;
      },
      error: (err) => console.error('Failed to load event overview', err)
    });
  }
}
