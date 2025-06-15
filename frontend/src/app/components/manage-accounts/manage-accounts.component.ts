import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserService} from 'src/app/services/user.service';
import {LockedUser} from 'src/app/dtos/locked-user';
import {AuthService} from 'src/app/services/auth.service';
import {User} from "../../dtos/user";
import {ToastrService} from "ngx-toastr";
import {Router, RouterLink} from "@angular/router";
import {Page} from "../../dtos/page";
import {ErrorFormatterService} from "../../services/error-formatter.service";

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
  usersPage?: Page<User>;
  usersCurrentPage = 0;
  usersPageSize = 10;
  usersLoading = false;
  usersTriggered = false;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private router: Router
  ) {
  }

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
  loadAllUsers(page: number = 0) {
    this.usersLoading = true;
    this.usersTriggered = true;
    this.error = false;
    this.errorMessage = '';

    this.userService.getAllUsersPaginated(page, this.usersPageSize).subscribe({
      next: (pageResult) => {
        this.usersPage = pageResult;
        this.usersCurrentPage = page;
        this.usersLoading = false;
      },
      error: (err) => {
        this.usersPage = undefined;
        this.usersLoading = false;
        this.usersTriggered = false;
        this.notification.error(this.errorFormatter.format(err), 'Loading users failed', {
          enableHtml: true,
          timeOut: 8000
        });
      }
    });
  }

  /**
   * Unlocks the specified user and refreshes the list
   */
  onUnlock(id: number) {
    this.userService.unlockUser(id).subscribe({
      next: () => {
        this.loadAllUsers(this.usersCurrentPage);
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
        this.loadAllUsers(this.usersCurrentPage)
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
        this.loadAllUsers(this.usersCurrentPage)
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
