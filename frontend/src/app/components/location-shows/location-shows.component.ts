import {Component, OnInit} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
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

    page = 0;
    pageSize = 5;
    totalPages = 0;

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
        this.locationService.getShowsForEventLocation(this.locationId, page, this.pageSize)
            .subscribe({
                next: pageResult => {
                    this.shows = pageResult.content;
                    this.page = pageResult.number;
                    this.totalPages = pageResult.totalPages;
                    this.loading = false;
                },
                error: () => {
                    this.errorMsg = 'Could not load Shows.';
                    this.loading = false;
                }
            });
    }
    prevPage(): void {
        if (this.page > 0) {
            this.loadShows(this.page - 1);
        }
    }

    nextPage(): void {
        if (this.page + 1 < this.totalPages) {
            this.loadShows(this.page + 1);
        }
    }
}
