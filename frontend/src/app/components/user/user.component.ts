import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {Router, RouterLink} from "@angular/router";
import {ToastrService} from 'ngx-toastr';
import {User} from "../../dtos/user";
import {Sex} from "../../dtos/sex";
import {UserService} from "../../services/user.service";
import {DatePipe, CommonModule} from "@angular/common";

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss'],
  standalone: true,
  imports: [DatePipe, RouterLink, CommonModule]
})

export class UserComponent implements OnInit {
  user: User;
  showDeleteConfirm = false;
  loading = false;

  constructor(
    public authService: AuthService,
    private service: UserService,
    private notification: ToastrService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.user = {
      id: null,
      dateOfBirth: new Date(),
      sex: Sex.male,
      email: "",
      housenumber: "",
      country: "",
      city: "",
      street: "",
      postalCode: "",
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

    this.service.deleteUser().subscribe({
      next: () => {
        this.notification.success('Account irreversibly deleted!', 'Deleted');
        this.authService.logoutUser();
        this.router.navigate(['/login']);

      },
      error: (error) => {
        this.notification.error('Error while deleting user.', 'Error');
        console.error(error);
      },
    });
  }

  onDeleteClick(): void {
    this.showDeleteConfirm = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
  }

  confirmDelete(): void {
    this.showDeleteConfirm = false;
    this.deleteUser();
  }

}
