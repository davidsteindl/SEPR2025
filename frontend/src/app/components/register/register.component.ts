import {Component} from '@angular/core';
import {ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";
import {NgIf} from "@angular/common";
import {RegisterUser} from "../../dtos/register-user";


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  imports: [
    NgIf,
    ReactiveFormsModule
  ],
  styleUrl: './register.component.scss'
})
export class RegisterComponent {

  registerForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  // Error flag
  error = false;
  errorMessage = '';
  registerd = false;

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private router: Router) {
    this.registerForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      dateOfBirth: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.maxLength(100), Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  /**
   * Form validation will start after the method is called, additionally an AuthRequest will be sent
   */
  registerUser() {
    this.submitted = true;
    if (this.registerForm.valid) {
      console.log("valid input");
      const registerUser: RegisterUser = {
        firstName: this.registerForm.controls.firstName.value,
        lastName: this.registerForm.controls.lastName.value,
        password: this.registerForm.controls.password.value,
        confirmPassword: this.registerForm.controls.confirmPassword.value,
        dateOfBirth: this.registerForm.controls.dateOfBirth.value,
        email: this.registerForm.controls.email.value,
      };
      this.regUser(registerUser);
    } else {
      console.log('Invalid input');
    }
  }

  /**
   * Send authentication data to the authService. If the authentication was successfully, the user will be forwarded to the message page
   *
   * @param registerUser authentication data from the user login form
   */
  regUser(registerUser: RegisterUser) {
    console.log("authenticate")
    console.log("Register payload", registerUser);
    this.authService.registerUser(registerUser).subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: error => {
        console.log('Could not register in due to:');
        console.log(error);
        this.error = true;
        if (typeof error.error === 'object') {
          this.errorMessage = error.error.error;
        } else {
          this.errorMessage = error.error;
        }
      }
    });
  }

  goToPage() {
    this.router.navigate(['/login']);
  }

}
