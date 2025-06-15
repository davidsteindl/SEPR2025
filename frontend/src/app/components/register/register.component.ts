import {Component} from '@angular/core';
import {ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../services/auth.service";
import {Router, RouterLink} from "@angular/router";
import {NgIf} from "@angular/common";
import {RegisterUser} from "../../dtos/register-user";
import {ToastrService} from "ngx-toastr";
import {EmailSentComponent} from "../password/email-sent/email-sent.component";
import {TermsandconditionsComponent} from "../termsandconditions/termsandconditions.component";


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    TermsandconditionsComponent
  ],
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {

  registerForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  // Error flag
  error = false;
  errorMessage = '';
  firstName = '';
  lastName = '';
  buttonDisabled = false;
  showConfirm = false;
  private initialFormValue: any;

  constructor(private formBuilder: UntypedFormBuilder, protected authService: AuthService, private router: Router, private notification: ToastrService) {
    this.registerForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      dateOfBirth: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.maxLength(100)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]],
      termsAccepted: [false, Validators.requiredTrue],
      sex: ['', Validators.required],
      isAdmin: [false]
    });

  }

  /**
   * Form validation will start after the method is called, additionally an AuthRequest will be sent
   */
  registerUser() {
    this.submitted = true;
    this.buttonDisabled = true;
    if (this.registerForm.valid) {
      console.log("valid input");
      const registerUser: RegisterUser = {
        firstName: this.registerForm.controls.firstName.value,
        lastName: this.registerForm.controls.lastName.value,
        password: this.registerForm.controls.password.value,
        confirmPassword: this.registerForm.controls.confirmPassword.value,
        dateOfBirth: this.registerForm.controls.dateOfBirth.value,
        email: this.registerForm.controls.email.value,
        termsAccepted: this.registerForm.controls.termsAccepted.value,
        sex: this.registerForm.controls.sex.value,
        isAdmin: this.registerForm.controls.isAdmin.value,
        isActivated: true
      };
      if (this.isAdmin()) {
        registerUser.isActivated = false;
      }
      this.firstName = this.registerForm.controls.firstName.value;
      this.lastName = this.registerForm.controls.lastName.value;
      this.regUser(registerUser);
    } else {
      console.log('Invalid input');
      this.buttonDisabled = false;
    }
  }

  /**
   * Send authentication data to the authService. If the authentication was successfully, the user will be forwarded to the message page
   *
   * @param registerUser authentication data from the user login form
   */
  regUser(registerUser: RegisterUser) {
    console.log("register")
    this.authService.registerUser(registerUser).subscribe({
      next: () => {
        this.notification.success(`User ${this.firstName} ${this.lastName}
           successfully created.`);
        this.goToPage();
      },
      error: error => {
        console.log(`Could not register because ${error.error.errors}`);
        console.log(registerUser);
        this.error = true;
        if (typeof error.error === 'object') {
          this.notification.error(`Validation of user failed because ${error.error.errors}`);
          this.buttonDisabled = false;
        } else {
          this.errorMessage = error.error;
        }
      }
    });
  }

  /**
   * Saves the initial form value to compare it later with the current form value
   */
  ngOnInit() {
    this.initialFormValue = this.registerForm.getRawValue();
  }

  /**
  * Returns true if the authenticated user is an admin
  */
  isAdmin(): boolean {
    return this.authService.getUserRole() === 'ADMIN';
  }

  /**
   * Exits page if no changes were made
   */
  onBackClick(): void {
    if (this.isUnchanged()) {
      this.goToPage();
    } else {
      this.showConfirm = true;
    }
  }

  /**
   * Stays on the page
   */
  stay(): void {
    this.showConfirm = false;
  }

  /**
   * Exits the page and navigates to the admin panel
   */
  exit(): void {
    this.showConfirm = false;
    this.goToPage();
  }

  /**
   * Checks if the form has not been changed since the initial load
   */
  private isUnchanged(): boolean {
    return JSON.stringify(this.initialFormValue) === JSON.stringify(this.registerForm.getRawValue());
  }


  /**
   * Sends the User to either the login Page or if an Admin is creating a new user, back to the admin panel
   */
  goToPage() {
    if(this.isAdmin()) {
      this.router.navigate(['/manage-accounts']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
