import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  standalone: false
})
export class HeaderComponent implements OnInit {

  constructor(public authService: AuthService) { }

  ngOnInit() {
  }

  /**
* Returns true if the authenticated user is an admin
*/
  isAdmin(): boolean {
    return this.authService.getUserRole() === 'ADMIN';
  }

}
