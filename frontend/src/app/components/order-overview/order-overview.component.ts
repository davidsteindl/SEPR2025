import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {Page} from "../../dtos/page";
import {OrderDto, OrderGroupDto} from 'src/app/dtos/order';
import {OrderService} from "../../services/order.service";
import {DatePipe} from "@angular/common";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {PdfExportService} from "../../services/pdf-export.service";
import {TicketService} from "../../services/ticket.service";


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
  activeTab: 'upcoming' | 'reservations' | 'past'  = 'upcoming';


  upcomingOrders?: Page<OrderGroupDto>
  reservations?: Page<OrderGroupDto>
  pastOrders?: Page<OrderGroupDto>
  refundedOrders?: Page<OrderGroupDto>

  upcomingPage = 0;
  reservationsPage = 0;
  pastPage = 0;
  refundedPage = 0;

  pageSize = 10;

  loading = false;

  constructor(private ticketService: TicketService, private router: Router,  private route: ActivatedRoute,
              private pdfService: PdfExportService ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const tab = params['tab'];
      if (tab === 'upcoming' || tab === 'reservations' || tab === 'past') {
        this.activeTab = tab;
      } else {
        this.activeTab = 'upcoming';
      }

      this.loadOrders(this.activeTab);
    });
  }
  setTab(tab: 'upcoming' | 'reservations' | 'past') {
    this.activeTab = tab;

    this.router.navigate([], {
      queryParams: { tab },
      queryParamsHandling: 'merge'
    });

    const alreadyLoaded = {
      'upcoming': this.upcomingOrders,
      'reservations': this.reservations,
      'past': this.pastOrders,
    }[tab];

    if (!alreadyLoaded) {
      this.loadOrders(tab);
    }
  }

  loadOrders(type: 'upcoming' | 'reservations' | 'past', page = 0) {
    this.loading = true;

    const isReservation = type === 'reservations';
    const past = type === 'past';

    this.ticketService.getOrderGroupsPaged(isReservation, past, page, this.pageSize).subscribe({
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

  exportInvoice(orderId: number): void {

    this.pdfService.exportInvoicePdf(orderId);
  }

  exportCancelInvoicePdf(orderId: number): void {

    this.pdfService.exportCancelInvoicePdf(orderId);
  }



}
