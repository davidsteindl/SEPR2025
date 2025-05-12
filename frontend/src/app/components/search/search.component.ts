import { Component, OnInit } from '@angular/core';
import { FormsModule } from "@angular/forms";
import { CommonModule } from '@angular/common';
import { ArtistService } from '../../services/artist.service';
import {RouterLink} from "@angular/router";
import {ArtistSearchDto} from "../../dtos/artist";


@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink
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
    const searchDto: ArtistSearchDto = {
      firstname: this.firstname?.trim() || undefined,
      lastname: this.lastname?.trim() || undefined,
      stagename: this.stagename?.trim() || undefined
    };

    if (!searchDto.firstname && !searchDto.lastname && !searchDto.stagename) {
      return;
    }

    this.searchTriggered = true;

    this.artistService.searchArtists(searchDto).subscribe({
      next: (artists) => {
        this.results = artists;
      },
      error: (err) => {
        console.error('Error while searching for artist', err);
        this.results = [];
      }
    });
  }
}
