import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {Page} from "../../dtos/page";
import { OrderDto } from 'src/app/dtos/order';
import {OrderService} from "../../services/order.service";
import {CurrencyPipe, DatePipe} from "@angular/common";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-order-overview',
  templateUrl: './order-overview.component.html',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe
  ],
  styleUrls: ['./order-overview.component.scss']
})
export class OrderOverviewComponent implements OnInit {
  activeTab: 'upcoming' | 'reservations' | 'past' | 'refund' = 'upcoming';


  upcomingOrders?: Page<OrderDto>;
  reservations?: Page<OrderDto>;
  pastOrders?: Page<OrderDto>;
  refundOrders?: Page<OrderDto>;

  upcomingPage = 0;
  reservationsPage = 0;
  pastPage = 0;
  refundPage = 0;

  pageSize = 10;

  loading = false;

  constructor(private orderService: OrderService, private router: Router,  private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const tab = params['tab'];
      if (tab === 'upcoming' || tab === 'reservations' || tab === 'past' || tab === 'canceled') {
        this.activeTab = tab;
      } else {
        this.activeTab = 'upcoming'; // fallback
      }

      this.loadOrders(this.activeTab);
    });
  }
  setTab(tab: 'upcoming' | 'reservations' | 'past' | 'refund') {
    this.activeTab = tab;

    this.router.navigate([], {
      queryParams: { tab },
      queryParamsHandling: 'merge'
    });

    const alreadyLoaded = {
      'upcoming': this.upcomingOrders,
      'reservations': this.reservations,
      'past': this.pastOrders,
      'refund': this.refundOrders
    }[tab];

    if (!alreadyLoaded) {
      this.loadOrders(tab);
    }
  }

  loadOrders(type: 'upcoming' | 'reservations' | 'past' | 'refund', page = 0) {

    this.loading = true;

    const mockOrders: OrderDto[] = Array.from({ length: 5 }, (_, i) => ({
      id: i + 1 + page * 5,
      createdAt: new Date().toISOString(),
      tickets: [],
      paymentType: 'CREDIT_CARD',
      userId: 42,
      orderType: type === 'reservations' ? 'RESERVATION' : 'ORDER',
      totalPrice: 1999,
      showName: `Mock Show ${i + 1}`,
      showDate: new Date(Date.now() + i * 86400000).toISOString(), // heute + i Tage
      location: `Mock Location ${i + 1}`
    }));

    const mockPage: Page<OrderDto> = {
      content: mockOrders,
      totalElements: 20,
      totalPages: 4,
      number: page,
      size: 5
    };
    console.log('Mock orders:', mockOrders);


    setTimeout(() => {
      switch (type) {
        case 'upcoming':
          this.upcomingOrders = mockPage;
          this.upcomingPage = page;
          break;
        case 'reservations':
          this.reservations = mockPage;
          this.reservationsPage = page;
          break;
        case 'past':
          this.pastOrders = mockPage;
          this.pastPage = page;
          break;
        case 'refund':
          this.refundOrders = mockPage;
          this.refundPage = page;
          break;
      }
      this.loading = false;
    }, 300);



    /*
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
     */
  }
}
