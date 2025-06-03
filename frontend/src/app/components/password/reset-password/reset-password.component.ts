import { Component } from '@angular/core';
import {EmailSentComponent} from "../email-sent/email-sent.component";
import {NgIf} from "@angular/common";
import {FormGroup, ReactiveFormsModule, UntypedFormBuilder, Validators} from "@angular/forms";
import {Router, RouterLink} from "@angular/router";

@Component({
  selector: 'app-reset-password',
    imports: [
        EmailSentComponent,
        NgIf,
        ReactiveFormsModule,
        RouterLink
    ],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent {
  passwordResetForm: FormGroup;
  submitted = false;
  isSubmitting = false;

  constructor(private formBuilder: UntypedFormBuilder, private router: Router) {
    this.passwordResetForm = this.formBuilder.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  resetPassword(){

  }



}
