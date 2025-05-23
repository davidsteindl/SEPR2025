import {Component, OnInit} from '@angular/core';
import {Page} from "../../dtos/page";
import { OrderDto } from 'src/app/dtos/order';
import {OrderService} from "../../services/order.service";
import {CurrencyPipe, DatePipe} from "@angular/common";

@Component({
  selector: 'app-order-overview',
  templateUrl: './order-overview.component.html',
  standalone: true,
  imports: [
    DatePipe,
    CurrencyPipe
  ],
  styleUrls: ['./order-overview.component.scss']
})
export class OrderOverviewComponent implements OnInit {
  activeTab: 'upcoming' | 'reservations' | 'past' = 'upcoming';


  upcomingOrders?: Page<OrderDto>;
  reservations?: Page<OrderDto>;
  pastOrders?: Page<OrderDto>;

  upcomingPage = 0;
  reservationsPage = 0;
  pastPage = 0;

  pageSize = 10;

  loading = false;

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders('upcoming');
  }

  setTab(tab: 'upcoming' | 'reservations' | 'past') {
    this.activeTab = tab;
    const alreadyLoaded = {
      'upcoming': this.upcomingOrders,
      'reservations': this.reservations,
      'past': this.pastOrders
    }[tab];

    if (!alreadyLoaded) {
      this.loadOrders(tab);
    }
  }

  loadOrders(type: 'upcoming' | 'reservations' | 'past', page = 0) {
    this.loading = true;
    this.orderService.getOrders(type, page, this.pageSize).subscribe({
      next: (res) => {
        switch (type) {
          case 'upcoming':
            this.upcomingOrders = res;
            this.upcomingPage = res.number;
            break;
          case 'reservations':
            this.reservations = res;
            this.reservationsPage = res.number;
            break;
          case 'past':
            this.pastOrders = res;
            this.pastPage = res.number;
            break;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
