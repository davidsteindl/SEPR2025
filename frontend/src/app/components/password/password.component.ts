import {Component} from '@angular/core';
import {NgIf} from "@angular/common";
import {FormGroup, ReactiveFormsModule, UntypedFormBuilder, Validators} from "@angular/forms";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-password',
  imports: [
    ReactiveFormsModule,
    NgIf,
    RouterLink
  ],
  templateUrl: './password.component.html',
  styleUrl: './password.component.scss'
})
export class PasswordComponent {
  passwordResetForm: FormGroup;
  submitted = false;

  constructor(private formBuilder: UntypedFormBuilder) {
    this.passwordResetForm = this.formBuilder.group({
      email: ['', [Validators.required]]
    });
  }


  resetPassword() {
    this.submitted = true;
  }

}
