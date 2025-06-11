import {Component, OnInit} from '@angular/core';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable} from 'rxjs';
import {Sex} from 'src/app/dtos/sex';
import {formatIsoDate} from "../../../utils/date-helper";
import {convertFromUserToEdit, User} from 'src/app/dtos/user';
import {UserService} from "../../../services/user.service";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {AuthService} from "../../../services/auth.service";

@Component({
  selector: 'app-user-edit',
  templateUrl: './user-edit.component.html',
  imports: [
    FormsModule,
    FormsModule
  ],
  standalone: true,
  styleUrls: ['./user-edit.component.scss']
})
export class UserEditComponent implements OnInit {
  loading = false;

  email : string = '';

  user: User = {
    id: null,
    firstName: '',
    lastName: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    email: '',
    housenumber: "",
    country: "",
    city: "",
    street: ""
  };
  userBirthDateIsSet = false;

  constructor(
    private service: UserService,
    public authService: AuthService,
    private router: Router,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

  public get submitButtonText(): string {
      return 'Save changes'
  }

  public get userBirthDateText(): string {
      return formatIsoDate(this.user.dateOfBirth);
  }

  public set userBirthDateText(date: string) {
    if (date == null || date === '') {
      this.userBirthDateIsSet = false;
    } else {
      this.userBirthDateIsSet = true;
      this.user.dateOfBirth = new Date(date);
    }
  }

  ngOnInit(): void {

    this.user = {
      id: null,
      dateOfBirth: new Date(),
      sex: Sex.female,
      email: "",
      firstName: "",
      lastName: "",
      housenumber: "",
      country: "",
      postalCode: "",
      city: "",
      street: ""
    };

    this.loadUserData();
  }

  private loadUserData(): void {
    this.loading = true;
    this.service.getCurrentUser().subscribe({
      next: (user) => {
        if (user) {
          this.user = user;

        } else {
          this.notification.error('User not found!', 'Error');
          this.router.navigate(['/users']);
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading user data', err);
        this.notification.error('Could not load user data', 'Error');
        this.loading = false;
      },
    });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.user);
    if (form.valid) {
      if (this.user.housenumber === '') {
        delete this.user.housenumber;
      }
      if (this.user.city === '') {
        delete this.user.city;
      }
      if (this.user.street === '') {
        delete this.user.street;
      }
      if (this.user.country === '') {
        delete this.user.country;
      }
      if (this.user.postalCode === '') {
        delete this.user.postalCode;
      }

      let observable: Observable<void>;

        if (this.service.getCurrentUser()) {

            observable = this.service.edit(
              convertFromUserToEdit(this.user));
          } else {
          console.error('No user email provided for editing');
          return;
        }

      observable.subscribe({
        next: data => {
           this.notification.success(`User ${this.user.firstName}
           successfully updated.`);
            this.router.navigate(['/user']);
        },
        error: error => {
           console.error('Error saving user', error);
           this.notification.error(this.errorFormatter.format(error), 'Could Not Save User', {
             enableHtml: true,
             timeOut: 10000 });
        }
      });
    }
  }

}
