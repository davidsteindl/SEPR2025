import { Component } from '@angular/core';
import {Location, NgIf} from "@angular/common";
import {FormGroup, ReactiveFormsModule, UntypedFormBuilder, Validators} from "@angular/forms";
import {PasswordChange} from "../../../dtos/password-change";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../../services/auth.service";
import {ToastrService} from "ngx-toastr";


@Component({
  selector: 'app-account-activation',
  imports: [
    NgIf,
    ReactiveFormsModule
  ],
  templateUrl: './account-activation.component.html',
  styleUrl: './account-activation.component.scss'
})
export class AccountActivationComponent {

  accountActivationForm: FormGroup;
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
    this.accountActivationForm = this.formBuilder.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]]
    });
  }

  changePassword(){

    const fullPath = this.location.path();
    console.log('fullPath from Location:', fullPath);
    this.token = fullPath.substring(fullPath.lastIndexOf('/') + 1);
    console.log('Token extracted manually:', this.token);

    const activateAccountRequest: PasswordChange = {
      password: this.accountActivationForm.controls.password.value,
      confirmPassword: this.accountActivationForm.controls.confirmPassword.value,
      otToken: this.token
    }
    console.log(activateAccountRequest);

    this.authService.changePassword(activateAccountRequest, this.token).subscribe({
      next: () => {
        this.notification.success(`Account successfully activated`);
        console.log('Account successfully activated');
        this.goToLogin();
      },
      error: err => {
        this.notification.error(`Account-Activation failed because ${err.error.errors}`);
        console.error('Error activating account:', err.error);
      }
    });

  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

}
