import { Component, OnInit } from '@angular/core';
import { FormsModule } from "@angular/forms";
import { CommonModule } from '@angular/common';
import { ArtistService } from '../../services/artist.service';
import {RouterLink} from "@angular/router";
import {ArtistSearchDto, ArtistSearchResultDto} from "../../dtos/artist";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {Page} from "../../dtos/page";


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

  //artist-variables
  firstname: string = '';
  lastname: string = '';
  stagename: string = '';
  artistPage?: Page<ArtistSearchResultDto>;
  artistLoading = false;
  artistTriggered = false;
  artistCurrentPage = 0;
  artistPageSize = 10;

  ngOnInit(): void {
  }

  constructor(
    private artistService: ArtistService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

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


  searchArtists(page: number = 0): void {
    const searchDto: ArtistSearchDto = {
      firstname: this.firstname?.trim() || undefined,
      lastname: this.lastname?.trim() || undefined,
      stagename: this.stagename?.trim() || undefined,
      page: page,
      size: this.artistPageSize
    };

    this.artistLoading = true;
    this.artistTriggered = true;

    this.artistService.searchArtists(searchDto).subscribe({
      next: (pageResult) => {
        this.artistPage = pageResult;
        this.artistCurrentPage = pageResult.number;
        this.artistLoading = false;
      },
      error: (err) => {
        this.artistPage = undefined;
        this.artistLoading = false;
        this.artistTriggered = false;
        this.notification.error(this.errorFormatter.format(err), 'Search failed', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }
}
