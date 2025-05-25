import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {Page} from "../../dtos/page";
import { OrderDto } from 'src/app/dtos/order';
import {OrderService} from "../../services/order.service";
import {DatePipe} from "@angular/common";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";

@Component({
  selector: 'app-order-overview',
  templateUrl: './order-overview.component.html',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    RouterLink
  ],
  styleUrls: ['./order-overview.component.scss']
})
export class OrderOverviewComponent implements OnInit {
  activeTab: 'upcoming' | 'reservations' | 'past' | 'refunded' = 'upcoming';


  upcomingOrders?: Page<OrderDto>;
  reservations?: Page<OrderDto>;
  pastOrders?: Page<OrderDto>;
  refundedOrders?: Page<OrderDto>;

  upcomingPage = 0;
  reservationsPage = 0;
  pastPage = 0;
  refundedPage = 0;

  pageSize = 10;

  loading = false;

  constructor(private orderService: OrderService, private router: Router,  private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const tab = params['tab'];
      if (tab === 'upcoming' || tab === 'reservations' || tab === 'past' || tab === 'refunded') {
        this.activeTab = tab;
      } else {
        this.activeTab = 'upcoming';
      }

      this.loadOrders(this.activeTab);
    });
  }
  setTab(tab: 'upcoming' | 'reservations' | 'past' | 'refunded') {
    this.activeTab = tab;

    this.router.navigate([], {
      queryParams: { tab },
      queryParamsHandling: 'merge'
    });

    const alreadyLoaded = {
      'upcoming': this.upcomingOrders,
      'reservations': this.reservations,
      'past': this.pastOrders,
      'refunded': this.refundedOrders
    }[tab];

    if (!alreadyLoaded) {
      this.loadOrders(tab);
    }
  }

  loadOrders(type: 'upcoming' | 'reservations' | 'past' | 'refunded', page = 0) {

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
          case 'refunded':
            this.refundedOrders = res;
            this.refundedPage = res.number;
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
