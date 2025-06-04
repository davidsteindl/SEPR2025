import {Component, OnInit, ViewChild} from '@angular/core';
import {FormsModule, NgForm} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {CreateArtist} from '../../../dtos/create-artist';
import {ArtistService} from '../../../services/artist.service';
import {ShowService} from '../../../services/show.service';
import {Show} from '../../../dtos/show';
import {ToastrService} from 'ngx-toastr';
import {ErrorFormatterService} from '../../../services/error-formatter.service';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-create-artist',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
  ],
  templateUrl: './create-artist.component.html',
  styleUrl: './create-artist.component.scss'
})
export class CreateArtistComponent implements OnInit {
  @ViewChild('artistForm') form!: NgForm;

  artist: CreateArtist = {
    firstname: '',
    lastname: '',
    stagename: '',
    showIds: []
  };
  private initialArtist!: CreateArtist;

  shows: Show[] = [];

  constructor(
    private artistService: ArtistService,
    private showService: ShowService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.showService.getAll().subscribe({
      next: (result) => {
        this.shows = result;
      },
      error: (err) => {
        console.error('Error fetching shows:', err);
        this.notification.error(this.errorFormatter.format(err), 'Error while fetching shows', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });

    this.initialArtist= JSON.parse(JSON.stringify(this.artist));
  }

  isNameValid(): boolean {
    const a = this.artist;
    return !!a.stagename || (!!a.firstname && !!a.lastname);
  }

  isShowSelected(showId: number): boolean {
    return this.artist.showIds.includes(showId);
  }

  toggleShowSelection(showId: number): void {
    const index = this.artist.showIds.indexOf(showId);
    if (index > -1) {
      this.artist.showIds.splice(index, 1);
    } else {
      this.artist.showIds.push(showId);
    }
  }


  createArtist(): void {
    this.artistService.create(this.artist).subscribe({
      next: (createdArtist) => {
        if (createdArtist) {
          console.log('Artist created:', createdArtist);
          this.notification.success(`Artist ${createdArtist.stagename || createdArtist.firstname + ` ` + createdArtist.lastname} created successfully!`, 'Success');
          this.router.navigate(['/admin']);
        }
        this.artist = {
          firstname: '',
          lastname: '',
          stagename: '',
          showIds: []
        };
        this.form.resetForm();
        this.initialArtist = JSON.parse(JSON.stringify(this.artist));
      },
      error: (err) => {
        console.error('Error creating artist:', err);
        this.notification.error(this.errorFormatter.format(err), 'Error while creating artist', {
          enableHtml: true,
          timeOut: 8000,
        });
      }
    });
  }

  showConfirm: boolean = false;

  private isUnchanged(): boolean {
    return (
      JSON.stringify(this.initialArtist) === JSON.stringify(this.artist)
    );
  }

  onBackClick(): void {
    if (this.isUnchanged()) {
      this.router.navigate(['/admin']);
    } else {
      this.showConfirm = true;
    }
  }

  stay(): void {
    this.showConfirm = false;
  }

  exit(): void {
    this.showConfirm = false;
    this.router.navigate(['/admin']);
  }
}
