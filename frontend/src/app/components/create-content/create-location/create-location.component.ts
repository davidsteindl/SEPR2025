import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { LocationService } from '../../../services/location.service';
import {Location} from "../../../dtos/location";
import {CreateLocation} from "../../../dtos/create-location";

@Component({
  selector: 'app-create-location',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './create-location.component.html',
  styleUrl: './create-location.component.scss'
})
export class CreateLocationComponent {
  location: CreateLocation = {
    name: '',
    type: '',
    country: '',
    city: '',
    street: '',
    postalCode: ''
  };

  constructor(private locationService: LocationService) {}

  createdLocation: Location = null;
  locationTypes: string[] = [
    'Stadium',
    'Festival Ground',
    'Hall',
    'Opera',
    'Theater',
    'Club',
    'Other'
  ];

  createLocation() {
    this.locationService.create(this.location).subscribe({
      next: (response) => {
        this.createdLocation = response;
        this.location = {
          name: '',
          type: '',
          country: '',
          city: '',
          street: '',
          postalCode: ''
        };
      },
      error: (err) => {
        console.error('Error while creating location:', err);
      }
    });
  }
}
