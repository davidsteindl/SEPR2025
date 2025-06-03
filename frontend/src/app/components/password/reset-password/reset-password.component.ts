import {Component, OnInit} from '@angular/core';
import {EmailSentComponent} from "../email-sent/email-sent.component";
import {NgIf} from "@angular/common";
import {FormGroup, ReactiveFormsModule, UntypedFormBuilder, Validators} from "@angular/forms";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {AuthService} from "../../../services/auth.service";
import {RegisterUser} from "../../../dtos/register-user";
import {PasswordChange} from "../../../dtos/password-change";

@Component({
  selector: 'app-reset-password',
    imports: [
        NgIf,
        ReactiveFormsModule,
    ],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss'
})
export class ResetPasswordComponent implements OnInit{
  passwordResetForm: FormGroup;
  submitted = false;
  isSubmitting = false;

  constructor(private route: ActivatedRoute,
              private formBuilder: UntypedFormBuilder,
              private authService: AuthService,
              private router: Router) {
    this.passwordResetForm = this.formBuilder.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  changePassword(){

    const token = this.route.snapshot.queryParamMap.get('token');
    console.log('Token from URL:', token);

    const changePasswordRequest: PasswordChange = {
      password: this.passwordResetForm.controls.password.value,
      confirmPassword: this.passwordResetForm.controls.confirmPassword.value,
      otToken: token
    }
    console.log(changePasswordRequest);

    this.authService.changePassword(changePasswordRequest).subscribe({
      next: () => {
        console.log('Password changed successfully');
        this.goToLogin();
      },
      error: err => {
        console.error('Error changing password:', err);
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
