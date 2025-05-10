import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {Router, RouterLink} from "@angular/router";
import { ToastrService } from 'ngx-toastr';
import { User } from "../../dtos/user";
import {Sex} from "../../dtos/sex";
import {UserService} from "../../services/user.service";
import {DatePipe, NgIf} from "@angular/common";
import {ConfirmDeleteDialogComponent} from "../confirm-delete-dialog/confirm-delete-dialog.component";

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss'],
  imports: [DatePipe, RouterLink, ConfirmDeleteDialogComponent, NgIf]
})

export class UserComponent implements OnInit {
  user: User | undefined;
  userForDeletion: User | undefined;
  loading = false;

  constructor(
    public authService: AuthService,
    private service: UserService,
    private notification: ToastrService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.user = {
      dateOfBirth: new Date(),
      sex: Sex.female,
      email: "",
      address: "",
      paymentData: "",
      firstName: "",
      lastName: ""
    };

    this.loadUserData();
  }

  private loadUserData(): void {
    this.loading = true;
    this.service.getCurrentUser().subscribe({
      next: (user) => {
        if (user) {
          this.user = user;
    //     console.log(user.image);
        } else {
          this.notification.error('User not found!', 'Error');
          this.router.navigate(['/users']);
        }
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading user data', err);
        this.notification.error('Could not load user data', 'Error');
        this.loading = false;
      },
    });
  }


deleteUser(): void {
  if (this.user && this.user.id) {
  this.service.deleteUser(this.user.id).subscribe({
    next: () => {
      this.notification.success('Account irretiveble deleted!', 'Deleted');
      this.router.navigate(['/home']);
    },
    error: (error) => {
      this.notification.error('Error while deleting user.', 'Error');
    },
  });
}
}

}
