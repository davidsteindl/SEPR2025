import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {EventService} from '../../services/event.service';
import {Event} from '../../dtos/event';
import {Page} from "../../dtos/page";
import {CommonModule, NgForOf, NgIf} from "@angular/common";
import {Artist} from "../../dtos/artist";
import {ArtistService} from "../../services/artist.service";

@Component({
  selector: 'app-artist-events',
  standalone: true,
  imports: [
    CommonModule,
    NgForOf,
    NgIf,
    RouterLink
  ],
  templateUrl: './artist-events.component.html'
})
export class ArtistEventsComponent implements OnInit {
  artistId!: number;
  artistName = '';
  events: Event[] = [];
  page = 0;
  size = 5;
  totalPages = 0;

  constructor(private route: ActivatedRoute, private eventService: EventService, private artistService: ArtistService) {}

  ngOnInit(): void {
    this.artistId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadArtist();
    this.loadEvents();
  }

  loadArtist(): void {
    this.artistService.getArtistById(this.artistId).subscribe({
      next: (artist: Artist) => {
        this.artistName = artist.stagename || `${artist.firstname} ${artist.lastname}`;
      },
      error: err => {
        console.error('Loading artist failed', err);
        this.artistName = 'Unknown Artist';
      }
    });
  }

  loadEvents(): void {
    this.eventService.getEventsByArtist(this.artistId, this.page, this.size).subscribe({
      next: (data: Page<Event>) => {
        this.events = data.content;
        this.totalPages = data.totalPages;
      },
      error: err => console.error('Loading events failed', err)
    });
  }

  next(): void {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.loadEvents();
    }
  }

  prev(): void {
    if (this.page > 0) {
      this.page--;
      this.loadEvents();
    }
  }
}
