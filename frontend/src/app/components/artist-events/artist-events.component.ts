import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import { ArtistService } from '../../services/artist.service';
import { Artist } from '../../dtos/artist';

@Component({
  selector: 'app-artist-events',
  imports: [
    RouterLink
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
      error: (err) => console.error('Fehler beim Laden des Künstlers:', err)
    });
  }
}
