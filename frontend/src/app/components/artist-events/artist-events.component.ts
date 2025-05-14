import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import { ArtistService } from '../../services/artist.service';
import { Artist } from '../../dtos/artist';

@Component({
  selector: 'app-artist-events',
  standalone: true,
  imports: [
  ],
  templateUrl: './artist-events.component.html',
  styleUrl: './artist-events.component.scss'
})
export class ArtistEventsComponent implements OnInit{
  artist: Artist | null = null;
  events: Event[] = [];

  constructor(
    private route: ActivatedRoute,
    private artistService: ArtistService
  ) {}

  ngOnInit(): void {
    const artistId = Number(this.route.snapshot.paramMap.get('id'));
    this.artistService.getArtistById(artistId).subscribe({
      next: (artist) => {
        this.artist = artist;
        this.events = artist.shows?.map(show => show.event) ?? [];
      },
      error: (err) => console.error('Error while loading artist', err)
    });
  }
}
