import {Component, OnInit} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {CommonModule} from '@angular/common';
import {ArtistService} from '../../services/artist.service';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {ArtistSearchDto, ArtistSearchResultDto} from "../../dtos/artist";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {Page} from "../../dtos/page";
import {EventSearchDto, EventSearchResultDto} from "../../dtos/event";
import {EventService} from "../../services/event.service";
import {EventCategory} from "../create-content/create-event/create-event.component";
import {ShowSearch, ShowSearchResult} from "../../dtos/show";
import {ShowService} from "../../services/show.service";


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
  //tabs + reset
  private activeTab: 'artist' | 'location' | 'event' | 'performance' = 'artist';

  get currentActiveTab(): 'artist' | 'location' | 'event' | 'performance' {
    return this.activeTab;
  }

  set currentActiveTab(tab: 'artist' | 'location' | 'event' | 'performance') {
    this.activeTab = tab;

    if (tab !== 'artist') {
      this.firstname = '';
      this.lastname = '';
      this.stagename = '';
      this.artistPage = undefined;
      this.artistTriggered = false;
    }

    // Implement reset for tabs
    if (tab !== 'location') {
    }
    if (tab !== 'event') {
      this.eventname = '';
      this.eventcategory = null;
      this.eventduration = 0;
      this.eventdescription = '';
      this.artistPage = undefined;
      this.artistTriggered = false;
    }
    if (tab !== 'performance') {
    }
  }

  //artist variables
  firstname: string = '';
  lastname: string = '';
  stagename: string = '';

  artistPage?: Page<ArtistSearchResultDto>;
  artistLoading = false;
  artistTriggered = false;
  artistCurrentPage = 0;
  artistPageSize = 10;

  //event variables
  eventCategories =Object.values(EventCategory);

  eventname: string = '';
  eventcategory: EventCategory | null = null;
  eventduration: number | null = null;
  eventdescription: string = '';
  eventPage?: Page<EventSearchResultDto>;
  eventLoading = false;
  eventTriggered = false;
  eventCurrentPage = 0;
  eventPageSize = 10;


  // performance (Show) search
  showName: string = '';
  showEventName: string = '';
  showRoomName: string = '';
  showStartDate?: string;
  showEndDate?: string;
  showMinPrice?: number;
  showMaxPrice?: number;

  showPage?: Page<ShowSearchResult>;
  showLoading = false;
  showTriggered = false;
  showCurrentPage = 0;
  showPageSize = 10;

  ngOnInit(): void {
  }

  //constructor
  constructor(
    private artistService: ArtistService,
    private eventService: EventService,
    private showService: ShowService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

  //main search function
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
        this.searchEvents();
        break;
      case 'performance':
        this.searchShows();
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

  searchLocations(): void {
    // Implement location search
    console.log('Location search not implemented yet');
  }

  validateDuration(): void {
    if (this.eventduration == null) {
      return;
    }
    if (this.eventduration < 10) {
      this.eventduration = 10;
    } else if (this.eventduration > 10000) {
      this.eventduration = 10000;
    }
  }

  searchEvents(): void {
    const searchDto: EventSearchDto = {
      name: this.eventname?.trim() || undefined,
      category: this.eventcategory?.trim() || undefined,
      duration: this.eventduration || undefined,
      description: this.eventdescription?.trim() || undefined,
      page: this.eventCurrentPage,
      size: this.eventPageSize
    };

    this.eventLoading = true;
    this.eventTriggered = true;

    this.eventService.searchEvents(searchDto).subscribe({
      next: (pageResult) => {
        this.eventPage = pageResult;
        this.eventCurrentPage = pageResult.number;
        this.eventLoading = false;
      },
      error: (err) => {
        this.eventPage = undefined;
        this.eventLoading = false;
        this.eventTriggered = false;
        this.notification.error(this.errorFormatter.format(err), 'Search failed', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }

  searchShows(page: number = 0): void {
    const dto: ShowSearch = {
      page,
      size: this.showPageSize,
      name: this.showName?.trim() || undefined,
      eventName: this.showEventName?.trim() || undefined,
      roomName: this.showRoomName?.trim() || undefined,
      startDate: this.showStartDate ? new Date(this.showStartDate).toISOString() : undefined,
      endDate: this.showEndDate ? new Date(this.showEndDate).toISOString() : undefined,
      minPrice: this.showMinPrice,
      maxPrice: this.showMaxPrice
    };

    this.showLoading = true;
    this.showTriggered = true;

    this.showService.searchShows(dto).subscribe({
      next: (pageResult) => {
        this.showPage = pageResult;
        this.showCurrentPage = pageResult.number;
        this.showLoading = false;
      },
      error: (err) => {
        this.showPage = undefined;
        this.showLoading = false;
        this.showTriggered = false;
        this.notification.error(this.errorFormatter.format(err), 'Search failed', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }

}
