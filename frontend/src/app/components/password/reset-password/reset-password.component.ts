import {Component, OnInit} from '@angular/core';
import {EmailSentComponent} from "../email-sent/email-sent.component";
import {NgIf} from "@angular/common";
import {FormGroup, ReactiveFormsModule, UntypedFormBuilder, Validators} from "@angular/forms";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {AuthService} from "../../../services/auth.service";
import {RegisterUser} from "../../../dtos/register-user";
import {PasswordChange} from "../../../dtos/password-change";
import {ToastrService} from "ngx-toastr";
import { Location } from '@angular/common';


@Component({
  selector: 'app-reset-password',
  imports: [
    NgIf,
    ReactiveFormsModule,
  ],
  templateUrl: './reset-password.component.html',
  standalone: true,
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit{
  passwordResetForm: FormGroup;
  submitted = false;
  isSubmitting = false;
  token = '';

  constructor(private route: ActivatedRoute,
              private formBuilder: UntypedFormBuilder,
              private authService: AuthService,
              private router: Router,
              private notification: ToastrService,
              private location: Location) {
    this.location = location;
    this.passwordResetForm = this.formBuilder.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  changePassword(){

    const fullPath = this.location.path();
    console.log('fullPath from Location:', fullPath);
    this.token = fullPath.substring(fullPath.lastIndexOf('/') + 1);
    console.log('Token extracted manually:', this.token);

    const changePasswordRequest: PasswordChange = {
      password: this.passwordResetForm.controls.password.value,
      confirmPassword: this.passwordResetForm.controls.confirmPassword.value,
      otToken: this.token
    }
    console.log(changePasswordRequest);

    this.authService.changePassword(changePasswordRequest).subscribe({
      next: () => {
        this.notification.success(`Password changed successfully`);
        console.log('Password changed successfully');
        this.goToLogin();
      },
      error: err => {
        this.notification.error(`Password-Reset failed because ${err.error.errors}`);
        console.error('Error changing password:', err.error);
      }
    });

  }

  ngOnInit(): void {
    console.log('ResetPasswordComponent loaded');
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }


}
