import {Component, OnInit} from '@angular/core';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {Sex} from 'src/app/dtos/sex';
import {formatIsoDate} from "../../../utils/date-helper";
import {environment} from "../../../../environments/environment";
import {convertFromUserToEdit, User} from 'src/app/dtos/user';
import {UserService} from "../../../services/user.service";
import { AutocompleteComponent } from '../../autocomplete/autocomplete.component';
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {NgIf} from "@angular/common";

const baseUri = environment.backendUrl + '/users';

@Component({
  selector: 'app-user-edit',
  templateUrl: './user-edit.component.html',
  imports: [
    FormsModule,
    AutocompleteComponent,
    FormsModule,
    NgIf,
    RouterLink
  ],
  standalone: true,
  styleUrls: ['./user-edit.component.scss']
})
export class UserEditComponent implements OnInit {

  user: User = {
    firstName: '',
    lastName: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    email: '',
    address: '',
    paymentData: ''
  };
  userBirthDateIsSet = false;
  userId: string | null = null;


  constructor(
    private service: UserService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

  public get heading(): string {
        return 'Edit my Profile'
  }

  public get submitButtonText(): string {
      return 'Save changes'
  }

  public get userBirthDateText(): string {
    if (!this.userBirthDateIsSet) {
      return '';
    } else {
      return formatIsoDate(this.user.dateOfBirth);
    }
  }

  public set userBirthDateText(date: string) {
    if (date == null || date === '') {
      this.userBirthDateIsSet = false;
    } else {
      this.userBirthDateIsSet = true;
      this.user.dateOfBirth = new Date(date);
    }
  }

  get sex(): string {
    switch (this.user.sex) {
      case Sex.male:
        return 'Male';
      case Sex.female:
        return 'Female';
      default:
        return '';
    }
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.userId = params.get('id');
      if (this.userId) {
        this.loadUserData(this.userId);
      }
    });
  }

  private loadUserData(id: string): void {
    this.service.getById(id).subscribe({
      next: (user) => {
        if (user) {
          this.user = user;
          this.userBirthDateIsSet = !!this.user.dateOfBirth;
          console.log(user);
        } else {
          this.notification.error('Profile not found!', 'Error');
          this.router.navigate(['/user']);
        }

      },
      error: (err) => {
        console.error('Error loading user data', err);
        this.notification.error('Could not load profile data', 'Error');

      },
    });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public formatUserName(user: User | null | undefined): string {
    return (user == null)
      ? ''
      : `${user.lastName}`;
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.user);
    if (form.valid) {
      if (this.user.address === '') {
        delete this.user.address;
      }

      let observable: Observable<User>;
        if (this.userId) {
            observable = this.service.edit(this.userId,
              convertFromUserToEdit(this.user));
          } else {
          console.error('No user ID provided for editing');
          return;
        }

      observable.subscribe({
        next: data => {
           this.notification.success(`User ${this.user.firstName}
           successfully updated.`);
           this.router.navigate(['/users']);
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

