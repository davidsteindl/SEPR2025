import {Component} from '@angular/core';
import {NgIf} from "@angular/common";
import {FormGroup, ReactiveFormsModule, UntypedFormBuilder, Validators} from "@angular/forms";
import {Router, RouterLink} from "@angular/router";
import {EmailSentComponent} from "./email-sent/email-sent.component";


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
  isSubmitting = false;

  constructor(private formBuilder: UntypedFormBuilder, private router: Router) {
    this.passwordResetForm = this.formBuilder.group({
      email: ['', [Validators.required]]
    });
  }

  resetPassword() {
    this.submitted = true;

    if (this.passwordResetForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.emailSent = true;


    this.passwordResetForm.reset();
    this.submitted = false;
  }

}
