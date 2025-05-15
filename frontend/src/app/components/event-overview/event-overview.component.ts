import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event } from '../../dtos/event';
import { Show } from '../../dtos/show';
import { EventWithShows } from '../../dtos/event';
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


  constructor(private route: ActivatedRoute, private eventService: EventService, private locationService: LocationService, private artistService: ArtistService) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.eventService.getEventWithShows(id).subscribe({
      next: (data: EventWithShows) => {
        this.event = data.event;
        this.shows = data.shows;

        this.locationService.getLocationById(data.event.locationId).subscribe({
          next: loc => this.location = loc,
          error: err => console.error('Failed to load location', err)
        });

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
      error: (err) => console.error('Failed to load event overview', err)
    });

  }
}
