import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AuthRequest } from '../../dtos/auth-request';

interface IErrorMessage {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  timestamp: string;
  loginTries: number;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: false
})
export class LoginComponent implements OnInit {
  readonly MAX_ATTEMPTS = 5;
  loginForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  // Error flag
  error = false;
  errorMessage: IErrorMessage;
  remainingAttempts: number | null = null;
  validationErrors: string[] = [];

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private router: Router) {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(8)]]
    });
  }



  /**
   * Form validation will start after the method is called, additionally an AuthRequest will be sent
   */
  loginUser() {
    this.submitted = true;
    if (this.loginForm.valid) {
      const authRequest: AuthRequest = new AuthRequest(this.loginForm.controls.email.value, this.loginForm.controls.password.value);
      this.authenticateUser(authRequest);
    } else {
      console.log('Invalid input');
    }
  }

  /**
   * Send authentication data to the authService. If the authentication was successfully, the user will be forwarded to the message page
   *
   * @param authRequest authentication data from the user login form
   */
  authenticateUser(authRequest: AuthRequest) {
    this.authService.loginUser(authRequest).subscribe({
      next: () => {
        this.router.navigate(["/news"]);
      },
      error: err => {
        this.error = true;
        this.validationErrors = [];
        this.remainingAttempts = null;

        let payload: any = err.error;

        // If backend sent a JSON‐stringified array, parse it
        if (typeof payload === 'string') {
          try {
            payload = JSON.parse(payload);
          } catch {
            // leave payload as raw string if it wasn’t valid JSON
          }
        }

        // Case 1: validation errors array
        if (Array.isArray(payload)) {
          this.validationErrors = payload as string[];
          return;
        }

        // Case 2: login‐tries / 401 error
        const msg = payload as IErrorMessage;
        this.errorMessage = {
          type: msg.type,
          title: msg.title,
          status: err.status,
          detail: msg.detail,
          instance: msg.instance,
          timestamp: msg.timestamp,
          loginTries: msg.loginTries ?? 0
        };

        if (err.status === 401) {
          this.remainingAttempts = this.MAX_ATTEMPTS - this.errorMessage.loginTries;
        }
      }
    });
  }




  /**
   * Error flag will be deactivated, which clears the error message
   */
  vanishError() {
    this.error = false;
  }

  ngOnInit() {
  }


}
