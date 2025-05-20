import {Component, OnInit} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import {ShowSearchResult} from 'src/app/dtos/show';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {LocationService} from '../../services/location.service';


@Component({
  selector: 'app-location-shows',
  templateUrl: './location-shows.component.html',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  styleUrls: ['./location-shows.component.scss']
})
export class LocationShowsComponent implements OnInit {
  locationId!: number;

  shows: ShowSearchResult[] = [];
  loading = false;
  errorMsg: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private locationService: LocationService,
  ) {
  }

  ngOnInit(): void {
    this.locationId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadShows();
  }

  loadShows(page: number = 0): void {
    this.loading = true;
    this.errorMsg = null;
    this.locationService.getShowsForEventLocation(this.locationId)
      .subscribe({
        next: shows => {
          this.shows = shows;
          this.loading = false;
        },
        error: () => {
          this.errorMsg = 'Konnte Aufführungen nicht laden.';
          this.loading = false;
        }
      });
  }
}
