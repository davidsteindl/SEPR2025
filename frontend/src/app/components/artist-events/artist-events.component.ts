import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ArtistService} from '../../services/artist.service';
import {ShowService} from '../../services/show.service';
import {EventService} from '../../services/event.service';
import {Artist} from '../../dtos/artist';
import {Show} from '../../dtos/show';
import {Event} from '../../dtos/event';
import {ToastrService} from 'ngx-toastr';
import {ErrorFormatterService} from '../../services/error-formatter.service';

@Component({
  selector: 'app-artist-events',
  standalone: true,
  imports: [],
  templateUrl: './artist-events.component.html',
  styleUrl: './artist-events.component.scss'
})
export class ArtistEventsComponent implements OnInit {
  artist: Artist | null = null;
  events: Event[] = [];

  constructor(
    private route: ActivatedRoute,
    private artistService: ArtistService,
    private showService: ShowService,
    private eventService: EventService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

  ngOnInit(): void {
    const artistId = Number(this.route.snapshot.paramMap.get('id'));

    this.artistService.getArtistById(artistId).subscribe({
      next: (artist) => {
        this.artist = artist;

        if (artist.showIds?.length) {
          this.showService.getShowsByIds(artist.showIds).subscribe({
            next: (shows: Show[]) => {
              const eventIds = [...new Set(shows.map(show => show.eventId))];

              this.eventService.getEventsByIds(eventIds).subscribe({
                next: (events: Event[]) => {
                  this.events = events;
                },
                error: (err) => {
                  console.error('Error while fetching events', err)
                  this.notification.error(this.errorFormatter.format(err), 'Error while fetching events', {
                    enableHtml: true,
                    timeOut: 8000,
                  });
                }
              });
            },
            error: (err) => {
              console.error('Error while fetching shows', err)
              this.notification.error(this.errorFormatter.format(err), 'Error while fetching shows', {
                enableHtml: true,
                timeOut: 8000,
              });
            }
          });
        }
      },
      error: (err) => {
        console.error('Error while fetching artists', err)
        this.notification.error(this.errorFormatter.format(err), 'Error while fetching artists', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }
}
