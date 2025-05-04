import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from 'src/app/services/user.service';
import { LockedUser } from 'src/app/dtos/locked-user';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-manage-accounts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './manage-accounts.component.html',
  styleUrls: ['./manage-accounts.component.css'],
})
export class ManageAccountsComponent implements OnInit {
  error = false;
  errorMessage = '';
  lockedUsers: LockedUser[] = [];

  constructor(
    private userService: UserService,
    private authService: AuthService
  ) { }

  ngOnInit() {
    this.loadLockedUsers();
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
   * Unlocks the specified user and refreshes the list
   */
  onUnlock(id: number) {
    this.userService.unlockUser(id).subscribe({
      next: () => this.loadLockedUsers(),
      error: err => this.defaultServiceErrorHandling(err)
    });
  }

  /**
   * Hides the error alert
   */
  vanishError() {
    this.error = false;
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
