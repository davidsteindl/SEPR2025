import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../../services/auth.service";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-user-edit',
  templateUrl: './user-orders.component.html',
  standalone: true,
  imports: [
    RouterLink
  ],
  styleUrls: ['./user-orders.component.scss']
})

export class UserOrdersComponent implements OnInit {

  constructor(public authService: AuthService) { }

  ngOnInit() {
  }

}
