import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event } from '../../dtos/event';
import { Show } from '../../dtos/show';
import { LocationService } from '../../services/location.service';
import { Location } from '../../dtos/location';
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import { ArtistService } from '../../services/artist.service';
import { Artist } from '../../dtos/artist';

@Component({
  selector: 'app-event-overview',
  standalone: true,
  templateUrl: './event-overview.component.html',
  imports: [
    NgIf,
    NgForOf,
    DatePipe
  ]
})
export class EventOverviewComponent implements OnInit {
  event: Event | null = null;
  shows: Show[] = [];
  location: Location | null = null;
  artistMap: { [showId: number]: Artist[] } = {};
  page = 0;
  pageSize = 5;
  totalPages = 0;


  constructor(private route: ActivatedRoute,
              private eventService: EventService,
              private locationService: LocationService,
              private artistService: ArtistService
              ) {
  }

  ngOnInit(): void {
    const eventId = Number(this.route.snapshot.paramMap.get('id'));

    this.eventService.getEventById(eventId).subscribe({
      next: event => {
        this.event = event;

        this.locationService.getLocationById(event.locationId).subscribe({
          next: loc => this.location = loc,
          error: err => console.error('Failed to load location', err)
        });

        this.loadPagedShows(event.id);
      },
      error: err => console.error('Failed to load event', err)
    });
  }

  loadPagedShows(eventId: number): void {
    this.eventService.getPaginatedShowsForEvent(eventId, this.page, this.pageSize).subscribe({
      next: result => {
        this.shows = result.content;
        this.totalPages = result.totalPages;

        this.artistMap = {};
        this.shows.forEach(show => {
          this.artistMap[show.id] = [];
          show.artistIds.forEach(artistId => {
            this.artistService.getArtistById(artistId).subscribe({
              next: artist => this.artistMap[show.id].push(artist),
              error: err => console.error(`Failed to load artist ${artistId} for show ${show.id}`, err)
            });
          });
        });
      },
      error: err => console.error('Failed to load paged shows', err)
    });
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages && this.event) {
      this.page++;
      this.loadPagedShows(this.event.id);
    }
  }

  prevPage(): void {
    if (this.page > 0 && this.event) {
      this.page--;
      this.loadPagedShows(this.event.id);
    }
  }
}
