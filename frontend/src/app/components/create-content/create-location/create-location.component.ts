import {Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {LocationService} from '../../../services/location.service';
import {Location} from "../../../dtos/location";
import {CreateLocation} from "../../../dtos/create-location";
import {ToastrService} from 'ngx-toastr';
import {ErrorFormatterService} from '../../../services/error-formatter.service';
import { Router } from '@angular/router';

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

  constructor(
    private locationService: LocationService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private router: Router
  ) {
  }

  createdLocation: Location = null;
  locationTypeOptions = locationTypeOptions;

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

        if (response) {
          this.notification.success(`Location ${response.name} created successfully!`, 'Success', {
            enableHtml: true,
            timeOut: 8000,
          });
          this.router.navigate(['/admin']);
        }
      },
      error: (err) => {
        console.error('Error while creating location:', err);
        this.notification.error(this.errorFormatter.format(err), 'Error while creating location', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }
}

export enum LocationType {
  STADIUM = 'STADIUM',
  FESTIVAL_GROUND = 'FESTIVAL_GROUND',
  HALL = 'HALL',
  OPERA = 'OPERA',
  THEATER = 'THEATER',
  CLUB = 'CLUB',
  OTHER = 'OTHER'
}

export const LocationTypeDisplayNames: Record<LocationType, string> = {
  [LocationType.STADIUM]: 'Stadium',
  [LocationType.FESTIVAL_GROUND]: 'Festival Ground',
  [LocationType.HALL]: 'Hall',
  [LocationType.OPERA]: 'Opera',
  [LocationType.THEATER]: 'Theater',
  [LocationType.CLUB]: 'Club',
  [LocationType.OTHER]: 'Other'
};


export const locationTypeOptions = Object.values(LocationType).map((type) => ({
  value: type,
  label: LocationTypeDisplayNames[type]
}));
