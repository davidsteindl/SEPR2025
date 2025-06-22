import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ShowSearchResult} from 'src/app/dtos/show';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {LocationService} from '../../services/location.service';
import {Page} from "../../dtos/page";


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

  showPage?: Page<ShowSearchResult & { dateObj: Date }>;
  loading = false;
  showsTriggered = false;
  errorMsg: string | null = null;

  showCurrentPage = 0;
  pageSize = 5;
  now: Date = new Date();



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
    this.showsTriggered = true;
    this.errorMsg = null;

    this.locationService.getShowsForEventLocation(this.locationId, page, this.pageSize).subscribe({
      next: pageResult => {
        this.showPage = {
          ...pageResult,
          content: pageResult.content.map(s => ({
            ...s,
            dateObj: new Date(s.date)
          }))
        };
        this.showCurrentPage = pageResult.number;
        this.loading = false;
      },
      error: () => {
        this.errorMsg = 'Could not load Shows.';
        this.loading = false;
      }
    });
  }
}
