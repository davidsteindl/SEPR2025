import {Component} from '@angular/core';
import {NgIf} from "@angular/common";
import {FormGroup, ReactiveFormsModule, UntypedFormBuilder, Validators} from "@angular/forms";
import {Router, RouterLink} from "@angular/router";
import {EmailSentComponent} from "./email-sent/email-sent.component";
import {AuthService} from "../../services/auth.service";
import {ToastrService} from "ngx-toastr";


@Component({
  selector: 'app-password',
  imports: [
    ReactiveFormsModule,
    NgIf,
    RouterLink,
    EmailSentComponent
  ],
  templateUrl: './password.component.html',
  styleUrl: './password.component.scss'
})
export class PasswordComponent {
  passwordResetForm: FormGroup;
  submitted = false;
  emailSent = false;
  email= '';
  isSubmitting = false;
  error = false;
  errorMessage = '';

  constructor(private formBuilder: UntypedFormBuilder, private router: Router, private authService: AuthService, private notification: ToastrService) {
    this.passwordResetForm = this.formBuilder.group({
      email: ['', [Validators.required]]
    });
  }

  resetPassword(email: string) {

    console.log("resetPassword started");

    this.authService.resetPassword(email).subscribe({
      next: () => {
        this.submitted = true;
        this.email = email;
        this.emailSent = true;
        this.isSubmitting = true;
      },
      error: error => {
        console.log(`Could not send email because ${error.error.errors}`);
        this.error = true;
        this.passwordResetForm.reset();
        this.submitted = false;
        if (typeof error.error === 'object') {
          this.notification.error(`Validation of email failed because ${error.error.errors}`);
        } else {
          this.errorMessage = error.error;
        }
      }
    });





  }

}
