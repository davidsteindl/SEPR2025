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

        if (response) {
          this.notification.success(`Location ${response.name} created successfully!`, 'Success', {
            enableHtml: true,
            timeOut: 8000,
          });
          this.router.navigate(['/']);
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
