import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event } from '../../dtos/event';
import { Show } from '../../dtos/show';
import { LocationService } from '../../services/location.service';
import { Location } from '../../dtos/location';
import {DatePipe, LowerCasePipe, NgForOf, NgIf, TitleCasePipe} from "@angular/common";
import { ArtistService } from '../../services/artist.service';
import { Artist } from '../../dtos/artist';
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";

@Component({
  selector: 'app-event-overview',
  standalone: true,
  templateUrl: './event-overview.component.html',
  imports: [
    NgIf,
    NgForOf,
    DatePipe,
    RouterLink,
    LowerCasePipe,
    TitleCasePipe
  ]
})
export class EventOverviewComponent implements OnInit {
  event: Event | null = null;
  shows: (Show & { dateObj: Date })[] = [];
  location: Location | null = null;
  artistMap: { [showId: number]: Artist[] } = {};
  backLink: any[] = ['/search'];
  backParams: any = { tab: 'event' };
  page = 0;
  pageSize = 5;
  totalPages = 0;
  now: Date = new Date();


  constructor(private route: ActivatedRoute,
              private eventService: EventService,
              private locationService: LocationService,
              private artistService: ArtistService,
              private notification: ToastrService,
              private errorFormatter: ErrorFormatterService
              ) {
  }

  ngOnInit(): void {
    const eventId = Number(this.route.snapshot.paramMap.get('id'));

    const qp = this.route.snapshot.queryParams;
    switch (qp['from']) {
      case 'artist':
        this.backLink = ['/artists', qp['artistId'], 'events'];
        this.backParams = {};
        break;
      case 'event':
        this.backLink = ['/search'];
        this.backParams = { tab: 'event' };
        break;
    }

    this.eventService.getEventById(eventId).subscribe({
      next: event => {
        this.event = event;

        this.locationService.getLocationById(event.locationId).subscribe({
          next: loc => this.location = loc,
          error: err => {
            this.notification.error(this.errorFormatter.format(err), 'Loading Location failed', {
              enableHtml: true,
              timeOut: 8000,
            });
          }
        });

        this.loadPagedShows(event.id);
      },
      error: err =>  {
        this.notification.error(this.errorFormatter.format(err), 'Loading events failed', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }

  loadPagedShows(eventId: number): void {
    this.eventService.getPaginatedShowsForEvent(eventId, this.page, this.pageSize).subscribe({
      next: result => {
        this.shows = result.content.map(show => ({
          ...show,
          dateObj: new Date(show.date)
        }));

        this.totalPages = result.totalPages;

        this.artistMap = {};
        this.shows.forEach(show => {
          this.artistMap[show.id] = [];

          show.artistIds.forEach(artistId => {
            this.artistService.getArtistById(artistId).subscribe({
              next: artist => this.artistMap[show.id].push(artist),
              error: err => {
                this.notification.error(this.errorFormatter.format(err), 'Artist ${artistId} for show ${show.id} failed', {
                  enableHtml: true,
                  timeOut: 8000,
                });
              }
            });
          });
        });
      },
      error: err => {
        this.notification.error(this.errorFormatter.format(err), 'Loading shows failed', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
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
