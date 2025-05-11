import { Component, OnInit } from '@angular/core';
import { FormsModule } from "@angular/forms";
import { CommonModule } from '@angular/common';
import { ArtistService } from '../../services/artist.service';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit {
  activeTab: 'artist' | 'location' | 'event' | 'performance' = 'artist';

  firstname: string = '';
  lastname: string = '';
  stagename: string = '';

  results: any[] = [];

  searchTriggered = false;

  ngOnInit(): void {
  }

  constructor(private artistService: ArtistService) {}


  search(): void {
    console.log('Search clicked');
    switch (this.activeTab) {
      case 'artist':
        this.searchArtists();
        break;
      case 'location':
        //this.searchLocations();
        break;
      case 'event':
       // this.searchEvents();
        break;
      case 'performance':
       // this.searchPerformances();
        break;
    }
  }

  searchArtists(): void {
    const query = this.stagename || this.firstname || this.lastname;
    if (!query.trim()) return;

    this.searchTriggered = true;

    this.artistService.searchArtists(query).subscribe({
      next: (artists) => {
        this.results = artists;
      },
      error: (err) => {
        console.error('Error while searching for artists:', err);
        this.results = [];
      }
    });
  }
}
