import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from 'src/app/services/user.service';
import { LockedUser } from 'src/app/dtos/locked-user';
import { AuthService } from 'src/app/services/auth.service';
import {User} from "../../dtos/user";
import {ToastrService} from "ngx-toastr";
import {Router, RouterLink} from "@angular/router";

@Component({
  selector: 'app-manage-accounts',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './manage-accounts.component.html',
  styleUrls: ['./manage-accounts.component.css'],
})
export class ManageAccountsComponent implements OnInit {
  error = false;
  errorMessage = '';
  lockedUsers: LockedUser[] = [];
  users: User[] = [];

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private notification: ToastrService,
    private router: Router
  ) { }

  ngOnInit() {
    this.loadAllUsers();
  }

  /**
   * Returns true if the authenticated user is an admin
   */
  isAdmin(): boolean {
    return this.authService.getUserRole() === 'ADMIN';
  }

  /**
   * Fetches the list of locked users
   */
  private loadLockedUsers() {
    this.userService.getLockedUsers().subscribe({
      next: users => {
        this.lockedUsers = users;
        console.log(this.lockedUsers)
      },
      error: err => this.defaultServiceErrorHandling(err)
    });
  }

  /**
   * Fetches the list of all users
   */
  private loadAllUsers() {
    this.userService.getAllUsers().subscribe({
      next: users => {
        this.users = users;
        console.log(this.users);
      },
      error: err => this.defaultServiceErrorHandling(err)
    });
  }

  /**
   * Unlocks the specified user and refreshes the list
   */
  onUnlock(id: number) {
    this.userService.unlockUser(id).subscribe({
      next: () => {
        this.loadAllUsers()
        this.notification.success(`User was unlocked.`);
      },
      error: err => this.defaultServiceErrorHandling(err)
    });
  }

  /**
   * Blocks the specified user and refreshes the list
   */
  block(id: number) {
    this.userService.blockUser(id).subscribe({
      next: () => {
        this.loadAllUsers()
        this.notification.success(`User was blocked.`);
      },
      error: err => this.defaultServiceErrorHandling(err)
    });
  }

  /**
   * Sends a Password-Reset to the user and refreshes the list
   */
  resetPassword(id: number) {
    this.userService.resetPassword(id).subscribe({
      next: () => {
        this.loadAllUsers()
        this.notification.success(`Password-Reset was sent.`);
      },
      error: err => this.defaultServiceErrorHandling(err)
    });
  }

  /**
   * Hides the error alert
   */
  vanishError() {
    this.error = false;
  }

  onBackClick() {
    this.router.navigate(['/admin']);
  }

  /**
   * Default error handler for service calls
   */
  private defaultServiceErrorHandling(error: any) {
    console.error(error);
    this.error = true;
    if (typeof error.error === 'string') {
      try {
        const parsed = JSON.parse(error.error);
        this.errorMessage = parsed.detail || error.error;
      } catch {
        this.errorMessage = error.error;
      }
    } else if (error.error && typeof error.error === 'object') {
      this.errorMessage = error.error.detail || JSON.stringify(error.error);
    } else {
      this.errorMessage = error.message || 'An unknown error occurred';
    }
  }
}
