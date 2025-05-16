import {Component} from '@angular/core';
import {ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../services/auth.service";
import {Router, RouterLink} from "@angular/router";
import {NgIf} from "@angular/common";
import {RegisterUser} from "../../dtos/register-user";
import {ToastrService} from "ngx-toastr";


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    RouterLink
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
  buttonDisabled = false;

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private router: Router, private notification: ToastrService) {
    this.registerForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      dateOfBirth: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.maxLength(100), Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]],
      termsAccepted: [false, Validators.requiredTrue],
      sex: ['', Validators.required]
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
        sex: this.registerForm.controls.sex.value
      };
      this.firstName = this.registerForm.controls.firstName.value;
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
        this.notification.success(`User ${this.firstName}
           successfully created.`);
        this.router.navigate(['/login']);
      },
      error: error => {
        console.log(`Could not register because ${error.error.errors}`);
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
}
