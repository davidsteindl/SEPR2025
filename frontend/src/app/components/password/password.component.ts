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

  resetPassword() {
    console.log("resetPassword started");
    this.email = this.passwordResetForm.controls.email.value;
    console.log(this.email);
    this.authService.resetPassword(this.email).subscribe({
      next: () => {
        this.submitted = true;
        this.emailSent = true;
        this.isSubmitting = true;
      },
      error: error => {
        console.log(`Could not send email because ${error}`);
        this.error = true;
        this.passwordResetForm.reset();
        this.submitted = false;
      }
    });





  }

}
