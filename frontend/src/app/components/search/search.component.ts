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
import {LocationService} from "../../services/location.service";
import {EventLocationSearchDto, Location as EventLocationDto} from "../../dtos/location";


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
  private activeTab: 'artist' | 'location' | 'event' | 'show' = 'artist';

  get currentActiveTab(): 'artist' | 'location' | 'event' | 'show' {
    return this.activeTab;
  }

  set currentActiveTab(tab: 'artist' | 'location' | 'event' | 'show') {
    this.activeTab = tab;
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

  //event location variables
  eventLocationName: string = '';
  eventLocationStreet: string = '';
  eventLocationCity: string = '';
  eventLocationCountry: string = '';
  eventLocationPostalCode: string = '';

  eventLocationPage?: Page<EventLocationDto>;
  eventLocationLoading = false;
  eventLocationTriggered = false;
  eventLocationCurrentPage = 0;
  eventLocationPageSize = 10;

  //event variables
  eventCategories = Object.values(EventCategory);

  eventName: string = '';
  eventCategory: EventCategory | null = null;
  eventDuration: number | null = null;
  eventDescription: string = '';

  eventPage?: Page<EventSearchResultDto>;
  eventLoading = false;
  eventTriggered = false;
  eventCurrentPage = 0;
  eventPageSize = 10;

  // show search
  showName: string = '';
  showEventName: string = '';
  showRoomName: string = '';
  showStartDate?: string;
  showEndDate?: string;
  showMinPrice: number | null = null;
  showMaxPrice: number | null = null;

  showPage?: Page<ShowSearchResult>;
  showLoading = false;
  showTriggered = false;
  showCurrentPage = 0;
  showPageSize = 10;

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const tab = params['tab'];
      if (tab === 'artist' || tab === 'location' || tab === 'event' || tab === 'show') {
        this.currentActiveTab = tab;
      }
    });
  }

  //constructor
  constructor(
    private artistService: ArtistService,
    private locationService: LocationService,
    private eventService: EventService,
    private showService: ShowService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private route: ActivatedRoute
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
        this.searchLocations();
        break;
      case 'event':
        this.searchEvents();
        break;
      case 'show':
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

  searchLocations(page:number=0): void {
    const searchDto: EventLocationSearchDto = {
      name: this.eventLocationName?.trim() || undefined,
      street: this.eventLocationStreet?.trim() || undefined,
      city: this.eventLocationCity || undefined,
      country: this.eventLocationCountry?.trim() || undefined,
      postalCode: this.eventLocationPostalCode?.trim() || undefined,
      page: page,
      size: this.eventLocationPageSize
    };

    this.eventLocationLoading = true;
    this.eventLocationTriggered = true;

    this.locationService.searchEventLocations(searchDto).subscribe({
      next: (pageResult) => {
        this.eventLocationPage = pageResult;
        this.eventLocationCurrentPage = page;
        this.eventLocationLoading = false;
      },
      error: (err) => {
        this.eventLocationPage = undefined;
        this.eventLocationLoading = false;
        this.eventLocationTriggered = false;
        this.notification.error(this.errorFormatter.format(err), 'Search failed', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }

  validateDuration(): void {
    if (this.eventDuration == null) {
      return;
    }
    if (this.eventDuration < 10) {
      this.eventDuration = 10;
    } else if (this.eventDuration > 10000) {
      this.eventDuration = 10000;
    }
  }

  searchEvents(page:number = 0): void {
    const searchDto: EventSearchDto = {
      name: this.eventName?.trim() || undefined,
      category: this.eventCategory?.trim() || undefined,
      duration: this.eventDuration || undefined,
      description: this.eventDescription?.trim() || undefined,
      page: page,
      size: this.eventPageSize
    };

    this.eventLoading = true;
    this.eventTriggered = true;

    this.eventService.searchEvents(searchDto).subscribe({
      next: (pageResult) => {
        this.eventPage = pageResult;
        this.eventCurrentPage = page;
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
  preventNonNumericInput(event: KeyboardEvent): void {
    const invalidChars = ['e', 'E', '+', '-', '.'];
    if (invalidChars.includes(event.key)) {
      event.preventDefault();
    }
  }
}
