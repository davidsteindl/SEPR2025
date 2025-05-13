import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ShowService } from '../../../services/show.service';
import { EventService } from '../../../services/event.service';
import { ArtistService } from '../../../services/artist.service';
import { CreateShow } from '../../../dtos/create-show';
import { Event } from '../../../dtos/event';
import { Artist } from '../../../dtos/artist';

@Component({
  selector: 'app-create-show',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './create-show.component.html',
  styleUrl: './create-show.component.scss'
})
export class CreateShowComponent implements OnInit {
  show: CreateShow = {
    name: '',
    duration: 60,
    date: '',
    eventId: null,
    artistIds: []
  };

  events: Event[] = [];
  artists: Artist[] = [];

  constructor(
    private showService: ShowService,
    private eventService: EventService,
    private artistService: ArtistService
  ) {}

  ngOnInit(): void {
    this.eventService.getAll().subscribe({
      next: (result) => this.events = result,
      error: (err) => console.error('Error fetching events:', err)
    });

    this.artistService.getAll().subscribe({
      next: (result) => this.artists = result,
      error: (err) => console.error('Error fetching artists:', err)
    });
  }

  createShow(): void {
    this.showService.create(this.show).subscribe({
      next: (createdShow) => {
        console.log('Show created:', createdShow);
        this.show = {
          name: '',
          duration: 60,
          date: '',
          eventId: null,
          artistIds: []
        };
      },
      error: (err) => {
        console.error('Error creating show:', err);
      }
    });
  }
}
